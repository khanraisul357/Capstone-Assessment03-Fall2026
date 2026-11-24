package com.tanvir.foodrescue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // used to expire offers that a shelter never answers
public class FoodRescueApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodRescueApplication.class, args);
    }
}
