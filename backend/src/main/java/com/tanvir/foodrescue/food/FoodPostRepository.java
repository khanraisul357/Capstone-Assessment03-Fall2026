package com.tanvir.foodrescue.food;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long> {
    List<FoodPost> findAllByOrderByPostedAtDesc();

    List<FoodPost> findByStatusAndOfferExpiresAtBefore(FoodStatus status, LocalDateTime time);

    List<FoodPost> findByAssignedToIdAndStatusInOrderByPostedAtDesc(Long shelterId, Collection<FoodStatus> statuses);
}
