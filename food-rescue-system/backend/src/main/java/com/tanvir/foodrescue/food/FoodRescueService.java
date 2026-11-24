package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodRescueService {

    private final FoodPostRepository foodPostRepository;
    private final ShelterRepository shelterRepository;
    private final QuickFoodParser parser;

    public FoodRescueService(FoodPostRepository foodPostRepository,
                             ShelterRepository shelterRepository,
                             QuickFoodParser parser) {
        this.foodPostRepository = foodPostRepository;
        this.shelterRepository = shelterRepository;
        this.parser = parser;
    }

    @Transactional
    public FoodPostResponseDTO createPost(CreateFoodRequest req) {
        if (req.getRawDescription() == null || req.getRawDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food description cannot be empty");
        }
        QuickFoodParser.ParsedFood parsed = parser.parse(req.getRawDescription());

        FoodPost post = new FoodPost();
        post.setRestaurantName(
                (req.getRestaurantName() == null || req.getRestaurantName().isBlank())
                        ? "Tanvir's Kitchen (Dhanmondi)"
                        : req.getRestaurantName().trim()
        );
        post.setRawDescription(req.getRawDescription().trim());
        post.setEstimatedPortions(parsed.estimatedPortions);
        post.setHasTrays(parsed.hasTrays);
        post.setHoldUntilText(parsed.holdUntilText);
        post.setHoldUntilMinutesFromMidnight(parsed.holdUntilMinutes);
        post.setPickupNote(parsed.pickupNote);
        post.setStatus(FoodStatus.OPEN);
        post.setPostedAt(LocalDateTime.now());

        FoodPost saved = foodPostRepository.save(post);
        return toDTO(saved, shelterRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<FoodPostResponseDTO> getAllPosts() {
        List<Shelter> shelters = shelterRepository.findAll();
        return foodPostRepository.findAllByOrderByPostedAtDesc()
                .stream()
                .map(post -> toDTO(post, shelters))
                .collect(Collectors.toList());
    }

    @Transactional
    public FoodPostResponseDTO claimFood(Long foodId, Long shelterId) {
        FoodPost post = foodPostRepository.findById(foodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food post not found"));

        if (post.getStatus() != FoodStatus.OPEN) {
            String holder = post.getClaimedBy() != null ? post.getClaimedBy().getName() : "another shelter";
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already claimed by " + holder + "! Locked to prevent double-trip."
            );
        }

        Shelter shelter = shelterRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        ShelterMatchDTO evaluation = evaluateShelter(post, shelter);
        if (!evaluation.isEligible()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Shelter cannot collect this batch: " + String.join("; ", evaluation.getWarnings())
            );
        }

        shelter.setTotalClaims(shelter.getTotalClaims() + 1);
        shelterRepository.save(shelter);

        post.setClaimedBy(shelter);
        post.setStatus(FoodStatus.CLAIMED);
        post.setClaimedAt(LocalDateTime.now());
        FoodPost saved = foodPostRepository.save(post);

        return toDTO(saved, shelterRepository.findAll());
    }

    @Transactional
    public FoodPostResponseDTO markPickedUp(Long foodId) {
        FoodPost post = foodPostRepository.findById(foodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food post not found"));

        if (post.getStatus() != FoodStatus.CLAIMED || post.getClaimedBy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food is not currently claimed");
        }

        Shelter shelter = post.getClaimedBy();
        shelter.setCompletedPickups(shelter.getCompletedPickups() + 1);
        shelterRepository.save(shelter);

        post.setStatus(FoodStatus.PICKED_UP);
        FoodPost saved = foodPostRepository.save(post);
        return toDTO(saved, shelterRepository.findAll());
    }

    @Transactional
    public FoodPostResponseDTO reportNoShow(Long foodId) {
        FoodPost post = foodPostRepository.findById(foodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food post not found"));

        if (post.getStatus() != FoodStatus.CLAIMED || post.getClaimedBy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active claim to mark as no-show");
        }

        Shelter shelter = post.getClaimedBy();
        shelter.setNoShows(shelter.getNoShows() + 1);
        shelterRepository.save(shelter);

        // Re-open food immediately so another reliable shelter can rescue it before spoilage
        post.setClaimedBy(null);
        post.setClaimedAt(null);
        post.setStatus(FoodStatus.OPEN);
        FoodPost saved = foodPostRepository.save(post);
        return toDTO(saved, shelterRepository.findAll());
    }

    private FoodPostResponseDTO toDTO(FoodPost post, List<Shelter> shelters) {
        FoodPostResponseDTO dto = new FoodPostResponseDTO();
        dto.setId(post.getId());
        dto.setRestaurantName(post.getRestaurantName());
        dto.setRawDescription(post.getRawDescription());
        dto.setEstimatedPortions(post.getEstimatedPortions());
        dto.setHasTrays(post.isHasTrays());
        dto.setHoldUntilText(post.getHoldUntilText());
        dto.setPickupNote(post.getPickupNote());
        dto.setStatus(post.getStatus());
        dto.setPostedAt(post.getPostedAt());
        dto.setClaimedAt(post.getClaimedAt());

        if (post.getClaimedBy() != null) {
            dto.setClaimedByShelterId(post.getClaimedBy().getId());
            dto.setClaimedByShelterName(post.getClaimedBy().getName());
            dto.setClaimedByReliability(post.getClaimedBy().getReliabilityScore());
        }

        List<ShelterMatchDTO> matches = shelters.stream()
                .map(s -> evaluateShelter(post, s))
                .sorted(Comparator.comparing(ShelterMatchDTO::isEligible).reversed()
                        .thenComparing(Comparator.comparingInt(ShelterMatchDTO::getMatchScore).reversed()))
                .collect(Collectors.toList());

        dto.setShelterMatches(matches);
        return dto;
    }

    private ShelterMatchDTO evaluateShelter(FoodPost post, Shelter shelter) {
        ShelterMatchDTO m = new ShelterMatchDTO();
        m.setShelterId(shelter.getId());
        m.setShelterName(shelter.getName());
        m.setArea(shelter.getArea());
        m.setTransportType(shelter.getTransportType());
        m.setCanTakeTrays(shelter.isCanTakeTrays());
        m.setMaxPortions(shelter.getMaxPortions());
        m.setLatestPickupFormatted(shelter.getLatestPickupFormatted());
        m.setReliabilityScore(shelter.getReliabilityScore());
        m.setNoShows(shelter.getNoShows());

        List<String> warnings = new ArrayList<>();
        boolean eligible = true;

        if (post.isHasTrays() && !shelter.isCanTakeTrays()) {
            eligible = false;
            warnings.add("Uses rickshaw — cannot carry catering trays");
        }
        if (post.getEstimatedPortions() > shelter.getMaxPortions()) {
            eligible = false;
            warnings.add("Exceeds shelter capacity (max " + shelter.getMaxPortions() + " portions)");
        }
        // If food is posted after shelter's cutoff window (e.g., shelter stops at 10:00 PM)
        int postedHourMin = post.getPostedAt() != null
                ? post.getPostedAt().getHour() * 60 + post.getPostedAt().getMinute()
                : 22 * 60 + 30;
        if (postedHourMin > shelter.getLatestPickupMinutesFromMidnight()
                && shelter.getLatestPickupMinutesFromMidnight() <= 22 * 60) {
            warnings.add("Usually collects before " + shelter.getLatestPickupFormatted());
        }

        int score = shelter.getReliabilityScore();
        if (shelter.getNoShows() > 0) {
            warnings.add(shelter.getNoShows() + " past no-show(s)");
            score -= shelter.getNoShows() * 10;
        }
        if (post.isHasTrays() && shelter.isCanTakeTrays()) {
            score += 10;
        }

        m.setEligible(eligible);
        m.setMatchScore(Math.max(0, score));
        m.setWarnings(warnings);
        return m;
    }
}
