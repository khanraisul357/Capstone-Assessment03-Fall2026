package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import com.tanvir.foodrescue.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Flow of one batch:
 *   post -> system picks the single best eligible shelter -> OFFERED (only that shelter sees it)
 *        -> shelter accepts -> ACCEPTED (ETA known) -> restaurant confirms PICKED_UP
 *        -> shelter declines / ignores the offer / does not show up -> next best shelter is offered
 *        -> nobody eligible -> UNMATCHED (restaurant knows early)
 */
@Service
public class FoodRescueService {

    private static final Logger log = LoggerFactory.getLogger(FoodRescueService.class);

    private final FoodPostRepository foodPostRepository;
    private final ShelterRepository shelterRepository;
    private final QuickFoodParser parser;
    private final int offerTimeoutMinutes;

    public FoodRescueService(FoodPostRepository foodPostRepository,
                             ShelterRepository shelterRepository,
                             QuickFoodParser parser,
                             @Value("${app.offer-timeout-minutes:5}") int offerTimeoutMinutes) {
        this.foodPostRepository = foodPostRepository;
        this.shelterRepository = shelterRepository;
        this.parser = parser;
        this.offerTimeoutMinutes = offerTimeoutMinutes;
    }

    // ------------------------------------------------------------------ restaurant side

    @Transactional
    public FoodPostResponseDTO createPost(CreateFoodRequest req) {
        if (req.getRawDescription() == null || req.getRawDescription().isBlank()) {
            throw bad("Describe what is left, e.g. \"30 plates rice and dal, hold until 11:30pm\"");
        }
        QuickFoodParser.ParsedFood parsed = parser.parse(req.getRawDescription());

        int readyAt;
        try {
            readyAt = (req.getReadyAt() == null || req.getReadyAt().isBlank())
                    ? TimeUtil.nowMinutes()
                    : TimeUtil.parseHHmm(req.getReadyAt());
        } catch (RuntimeException e) {
            throw bad("Ready time must look like 21:45");
        }

        FoodPost post = new FoodPost();
        post.setRestaurantName(
                (req.getRestaurantName() == null || req.getRestaurantName().isBlank())
                        ? "Tanvir's Restaurant (Dhanmondi)"
                        : req.getRestaurantName().trim());
        post.setRawDescription(req.getRawDescription().trim());
        post.setEstimatedPortions(parsed.estimatedPortions);
        post.setHasTrays(parsed.hasTrays);
        post.setReadyAtMinutes(readyAt);
        post.setHoldUntilMinutes(parsed.holdUntilMinutes);
        post.setPickupNote(parsed.pickupNote);
        post.setPostedAt(LocalDateTime.now());

        offerToBest(post, shelterRepository.findAll());
        return toDTO(foodPostRepository.save(post), shelterRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<FoodPostResponseDTO> getAllPosts() {
        List<Shelter> shelters = shelterRepository.findAll();
        return foodPostRepository.findAllByOrderByPostedAtDesc().stream()
                .map(p -> toDTO(p, shelters))
                .collect(Collectors.toList());
    }

    /** Restaurant confirms the shelter really collected the food. */
    @Transactional
    public FoodPostResponseDTO markPickedUp(Long foodId) {
        FoodPost post = load(foodId);
        if (post.getStatus() != FoodStatus.ACCEPTED || post.getAssignedTo() == null) {
            throw conflict("This batch has no accepted shelter to confirm");
        }
        Shelter shelter = post.getAssignedTo();
        shelter.setCompletedPickups(shelter.getCompletedPickups() + 1);
        shelterRepository.save(shelter);

        post.setStatus(FoodStatus.PICKED_UP);
        return toDTO(foodPostRepository.save(post), shelterRepository.findAll());
    }

    /** Shelter said yes but did not turn up: penalise it and immediately offer the food to the next one. */
    @Transactional
    public FoodPostResponseDTO reportNoShow(Long foodId) {
        FoodPost post = load(foodId);
        if (post.getStatus() != FoodStatus.ACCEPTED || post.getAssignedTo() == null) {
            throw conflict("No accepted shelter to mark as no-show");
        }
        Shelter shelter = post.getAssignedTo();
        shelter.setNoShows(shelter.getNoShows() + 1);
        shelterRepository.save(shelter);

        post.getExcludedShelterIds().add(shelter.getId());
        offerToBest(post, shelterRepository.findAll());
        return toDTO(foodPostRepository.save(post), shelterRepository.findAll());
    }

    /** Try matching again (e.g. after a shelter was added) for a batch nobody could take. */
    @Transactional
    public FoodPostResponseDTO rematch(Long foodId) {
        FoodPost post = load(foodId);
        if (post.getStatus() != FoodStatus.UNMATCHED) {
            throw conflict("Only unmatched batches can be matched again");
        }
        offerToBest(post, shelterRepository.findAll());
        return toDTO(foodPostRepository.save(post), shelterRepository.findAll());
    }

    // ------------------------------------------------------------------ shelter side

    @Transactional(readOnly = true)
    public List<FoodPostResponseDTO> getOffersForShelter(Long shelterId) {
        List<Shelter> shelters = shelterRepository.findAll();
        return foodPostRepository
                .findByAssignedToIdAndStatusInOrderByPostedAtDesc(
                        shelterId, List.of(FoodStatus.OFFERED, FoodStatus.ACCEPTED))
                .stream().map(p -> toDTO(p, shelters)).collect(Collectors.toList());
    }

    /** Only the shelter the batch is currently offered to may accept. Everyone else gets a 409. */
    @Transactional
    public FoodPostResponseDTO acceptOffer(Long foodId, Long shelterId) {
        FoodPost post = load(foodId);
        requireOfferedTo(post, shelterId);
        if (post.getOfferExpiresAt() != null && post.getOfferExpiresAt().isBefore(LocalDateTime.now())) {
            throw conflict("This offer has expired and is being passed to another shelter");
        }
        Shelter shelter = post.getAssignedTo();
        shelter.setTotalClaims(shelter.getTotalClaims() + 1);
        shelterRepository.save(shelter);

        post.setStatus(FoodStatus.ACCEPTED);
        post.setAcceptedAt(LocalDateTime.now());
        post.setOfferExpiresAt(null);
        post.setEtaMinutes(post.getReadyAtMinutes() + shelter.getTravelMinutes());
        return toDTO(foodPostRepository.save(post), shelterRepository.findAll());
    }

    @Transactional
    public FoodPostResponseDTO declineOffer(Long foodId, Long shelterId) {
        FoodPost post = load(foodId);
        requireOfferedTo(post, shelterId);
        post.getExcludedShelterIds().add(shelterId);
        offerToBest(post, shelterRepository.findAll());
        return toDTO(foodPostRepository.save(post), shelterRepository.findAll());
    }

    // ------------------------------------------------------------------ automatic expiry

    /** A shelter that ignores an offer must not block the food: pass it on after the timeout. */
    @Scheduled(fixedDelayString = "${app.expiry-check-ms:5000}")
    @Transactional
    public void expireStaleOffers() {
        List<FoodPost> stale = foodPostRepository
                .findByStatusAndOfferExpiresAtBefore(FoodStatus.OFFERED, LocalDateTime.now());
        if (stale.isEmpty()) return;
        List<Shelter> shelters = shelterRepository.findAll();
        for (FoodPost post : stale) {
            log.info("Offer for batch {} to {} timed out", post.getId(),
                    post.getAssignedTo() != null ? post.getAssignedTo().getName() : "?");
            if (post.getAssignedTo() != null) post.getExcludedShelterIds().add(post.getAssignedTo().getId());
            offerToBest(post, shelters);
            foodPostRepository.save(post);
        }
    }

    // ------------------------------------------------------------------ matching

    /** Picks the single best eligible shelter and offers the batch to it, otherwise marks it UNMATCHED. */
    private void offerToBest(FoodPost post, List<Shelter> shelters) {
        ShelterMatchDTO best = rank(post, shelters).stream()
                .filter(ShelterMatchDTO::isEligible)
                .findFirst().orElse(null);
        post.setEtaMinutes(null);
        post.setAcceptedAt(null);
        if (best == null) {
            post.setAssignedTo(null);
            post.setOfferExpiresAt(null);
            post.setStatus(FoodStatus.UNMATCHED);
            return;
        }
        Shelter chosen = shelters.stream()
                .filter(s -> s.getId().equals(best.getShelterId()))
                .findFirst().orElseThrow();
        post.setAssignedTo(chosen);
        post.setOfferExpiresAt(LocalDateTime.now().plusMinutes(offerTimeoutMinutes));
        post.setStatus(FoodStatus.OFFERED);
    }

    private List<ShelterMatchDTO> rank(FoodPost post, List<Shelter> shelters) {
        return shelters.stream()
                .map(s -> evaluate(post, s))
                .sorted(Comparator.comparing(ShelterMatchDTO::isEligible, Comparator.reverseOrder())
                        .thenComparing(ShelterMatchDTO::getMatchScore, Comparator.reverseOrder())
                        .thenComparingInt(ShelterMatchDTO::getTravelMinutes))
                .collect(Collectors.toList());
    }

    /** A shelter is eligible only if it can really collect THIS batch before the food spoils. */
    ShelterMatchDTO evaluate(FoodPost post, Shelter s) {
        List<String> warnings = new ArrayList<>();
        boolean eligible = true;

        Set<Long> excluded = post.getExcludedShelterIds();
        if (excluded != null && excluded.contains(s.getId())) {
            eligible = false;
            warnings.add("Already declined or did not show up for this batch");
        }
        if (post.isHasTrays() && !s.isCanTakeTrays()) {
            eligible = false;
            warnings.add("Cannot carry trays (" + s.getTransportType().toLowerCase() + ")");
        }
        if (post.getEstimatedPortions() > s.getMaxPortions()) {
            eligible = false;
            warnings.add("Over capacity (max " + s.getMaxPortions() + " portions)");
        }

        int arrive = post.getReadyAtMinutes() + s.getTravelMinutes();
        int deadline = Math.min(post.getHoldUntilMinutes(), s.getLatestPickupMinutes());
        if (arrive > deadline) {
            eligible = false;
            String why = post.getHoldUntilMinutes() <= s.getLatestPickupMinutes()
                    ? "food only holds until " + TimeUtil.format(post.getHoldUntilMinutes())
                    : "shelter collects only before " + TimeUtil.format(s.getLatestPickupMinutes());
            warnings.add("Would arrive " + TimeUtil.format(arrive) + " but " + why);
        }

        int reliability = s.getReliabilityScore();
        if (s.getNoShows() > 0) warnings.add(s.getNoShows() + " past no-show(s)");
        if (reliability < 60) warnings.add("Low reliability, ranked last");

        ShelterMatchDTO m = new ShelterMatchDTO();
        m.setShelterId(s.getId());
        m.setShelterName(s.getName());
        m.setArea(s.getArea());
        m.setTransportType(s.getTransportType());
        m.setCanTakeTrays(s.isCanTakeTrays());
        m.setMaxPortions(s.getMaxPortions());
        m.setLatestPickupFormatted(s.getLatestPickupFormatted());
        m.setTravelMinutes(s.getTravelMinutes());
        m.setArriveBy(TimeUtil.format(arrive));
        m.setReliabilityScore(reliability);
        m.setNoShows(s.getNoShows());
        m.setEligible(eligible);
        m.setMatchScore(Math.max(0, reliability - s.getTravelMinutes() / 2));
        m.setWarnings(warnings);
        return m;
    }

    // ------------------------------------------------------------------ helpers

    private void requireOfferedTo(FoodPost post, Long shelterId) {
        if (post.getStatus() != FoodStatus.OFFERED
                || post.getAssignedTo() == null
                || !post.getAssignedTo().getId().equals(shelterId)) {
            throw conflict("This batch is not offered to you (any more)");
        }
    }

    private FoodPost load(Long id) {
        return foodPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food post not found"));
    }

    private ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }

    private FoodPostResponseDTO toDTO(FoodPost post, List<Shelter> shelters) {
        FoodPostResponseDTO dto = new FoodPostResponseDTO();
        dto.setId(post.getId());
        dto.setRestaurantName(post.getRestaurantName());
        dto.setRawDescription(post.getRawDescription());
        dto.setEstimatedPortions(post.getEstimatedPortions());
        dto.setHasTrays(post.isHasTrays());
        dto.setReadyAtText(TimeUtil.format(post.getReadyAtMinutes()));
        dto.setHoldUntilText(TimeUtil.format(post.getHoldUntilMinutes()));
        dto.setPickupNote(post.getPickupNote());
        dto.setStatus(post.getStatus());
        dto.setPostedAt(post.getPostedAt());

        if (post.getAssignedTo() != null) {
            dto.setAssignedShelterId(post.getAssignedTo().getId());
            dto.setAssignedShelterName(post.getAssignedTo().getName());
            dto.setAssignedShelterReliability(post.getAssignedTo().getReliabilityScore());
        }
        if (post.getStatus() == FoodStatus.OFFERED && post.getOfferExpiresAt() != null) {
            dto.setOfferSecondsLeft(Math.max(0,
                    Duration.between(LocalDateTime.now(), post.getOfferExpiresAt()).getSeconds()));
        }
        if (post.getEtaMinutes() != null) {
            dto.setEtaText(TimeUtil.format(post.getEtaMinutes()));
        }
        dto.setShelterMatches(rank(post, shelters));
        return dto;
    }
}
