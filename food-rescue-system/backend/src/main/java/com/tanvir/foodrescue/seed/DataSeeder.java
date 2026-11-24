package com.tanvir.foodrescue.seed;

import com.tanvir.foodrescue.food.CreateFoodRequest;
import com.tanvir.foodrescue.food.FoodRescueService;
import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ShelterRepository shelterRepository;
    private final FoodRescueService foodRescueService;

    public DataSeeder(ShelterRepository shelterRepository, FoodRescueService foodRescueService) {
        this.shelterRepository = shelterRepository;
        this.foodRescueService = foodRescueService;
    }

    @Override
    public void run(String... args) {
        if (shelterRepository.count() == 0) {
            // 1. Van shelter - handles trays, high capacity, very reliable
            shelterRepository.save(new Shelter(
                    "Alo Community Kitchen (Van)",
                    "Dhanmondi 15",
                    "VAN",
                    true,
                    120,
                    23 * 60 + 59, // 11:59 PM
                    12, 12, 0
            ));

            // 2. Rickshaw shelter - max 50 portions, cannot take trays, reliable
            shelterRepository.save(new Shelter(
                    "Shobuj Youth Shelter",
                    "Jigatola",
                    "RICKSHAW",
                    false,
                    50,
                    23 * 60 + 30, // 11:30 PM
                    10, 9, 1
            ));

            // 3. Early cutoff shelter - can collect anything but only before 10:00 PM
            shelterRepository.save(new Shelter(
                    "Early Night Care Home",
                    "Kalabagan",
                    "VAN",
                    true,
                    100,
                    22 * 60, // 10:00 PM
                    8, 8, 0
            ));

            // 4. Unreliable shelter - history of saying yes and not turning up
            shelterRepository.save(new Shelter(
                    "Metro Night Relief",
                    "Mohammadpur",
                    "VAN",
                    true,
                    80,
                    23 * 60 + 45, // 11:45 PM
                    6, 3, 3
            ));
        }

        // Seed the exact example from Tanvir's scenario
        CreateFoodRequest sample = new CreateFoodRequest();
        sample.setRestaurantName("Tanvir's Restaurant (Dhanmondi)");
        sample.setRawDescription("about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate");
        foodRescueService.createPost(sample);
    }
}
