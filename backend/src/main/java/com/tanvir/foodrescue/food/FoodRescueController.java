package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FoodRescueController {

    private final FoodRescueService foodRescueService;
    private final ShelterRepository shelterRepository;

    public FoodRescueController(FoodRescueService foodRescueService,
                                ShelterRepository shelterRepository) {
        this.foodRescueService = foodRescueService;
        this.shelterRepository = shelterRepository;
    }

    @GetMapping("/shelters")
    public List<Shelter> getShelters() {
        return shelterRepository.findAll();
    }

    @GetMapping("/foods")
    public List<FoodPostResponseDTO> getAllFoods() {
        return foodRescueService.getAllPosts();
    }

    @PostMapping("/foods")
    public FoodPostResponseDTO postFood(@RequestBody CreateFoodRequest request) {
        return foodRescueService.createPost(request);
    }

    @PostMapping("/foods/{foodId}/claim/{shelterId}")
    public FoodPostResponseDTO claimFood(@PathVariable Long foodId, @PathVariable Long shelterId) {
        return foodRescueService.claimFood(foodId, shelterId);
    }

    @PostMapping("/foods/{foodId}/pickup")
    public FoodPostResponseDTO confirmPickup(@PathVariable Long foodId) {
        return foodRescueService.markPickedUp(foodId);
    }

    @PostMapping("/foods/{foodId}/noshow")
    public FoodPostResponseDTO reportNoShow(@PathVariable Long foodId) {
        return foodRescueService.reportNoShow(foodId);
    }
}
