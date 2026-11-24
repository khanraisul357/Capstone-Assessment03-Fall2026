package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FoodRescueController {

    private final FoodRescueService service;
    private final ShelterRepository shelterRepository;

    public FoodRescueController(FoodRescueService service, ShelterRepository shelterRepository) {
        this.service = service;
        this.shelterRepository = shelterRepository;
    }

    // ---- shelters
    @GetMapping("/shelters")
    public List<Shelter> getShelters() {
        return shelterRepository.findAll();
    }

    /** What a shelter sees: only batches offered to it or accepted by it. */
    @GetMapping("/shelters/{shelterId}/offers")
    public List<FoodPostResponseDTO> getOffers(@PathVariable Long shelterId) {
        return service.getOffersForShelter(shelterId);
    }

    // ---- restaurant
    @GetMapping("/foods")
    public List<FoodPostResponseDTO> getAllFoods() {
        return service.getAllPosts();
    }

    @PostMapping("/foods")
    public FoodPostResponseDTO postFood(@RequestBody CreateFoodRequest request) {
        return service.createPost(request);
    }

    @PostMapping("/foods/{foodId}/pickup")
    public FoodPostResponseDTO confirmPickup(@PathVariable Long foodId) {
        return service.markPickedUp(foodId);
    }

    @PostMapping("/foods/{foodId}/noshow")
    public FoodPostResponseDTO reportNoShow(@PathVariable Long foodId) {
        return service.reportNoShow(foodId);
    }

    @PostMapping("/foods/{foodId}/rematch")
    public FoodPostResponseDTO rematch(@PathVariable Long foodId) {
        return service.rematch(foodId);
    }

    // ---- shelter answers the offer
    @PostMapping("/foods/{foodId}/accept/{shelterId}")
    public FoodPostResponseDTO accept(@PathVariable Long foodId, @PathVariable Long shelterId) {
        return service.acceptOffer(foodId, shelterId);
    }

    @PostMapping("/foods/{foodId}/decline/{shelterId}")
    public FoodPostResponseDTO decline(@PathVariable Long foodId, @PathVariable Long shelterId) {
        return service.declineOffer(foodId, shelterId);
    }
}
