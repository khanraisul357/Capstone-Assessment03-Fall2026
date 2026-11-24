package com.tanvir.foodrescue.food;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long> {
    List<FoodPost> findAllByOrderByPostedAtDesc();
}
