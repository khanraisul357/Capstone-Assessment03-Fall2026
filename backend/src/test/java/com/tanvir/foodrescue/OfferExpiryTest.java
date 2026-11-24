package com.tanvir.foodrescue;

import com.tanvir.foodrescue.food.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A shelter that never answers must not block the food: the offer keeps moving until nobody is left. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:expirytest;DB_CLOSE_DELAY=-1",
        "app.offer-timeout-minutes=0",
        "app.expiry-check-ms=200"
})
class OfferExpiryTest {

    @Autowired FoodRescueService service;

    @Test
    void unansweredOffersAreReOfferedUntilUnmatched() throws Exception {
        CreateFoodRequest r = new CreateFoodRequest();
        r.setRawDescription("about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate");
        r.setReadyAt("21:45");
        FoodPostResponseDTO created = service.createPost(r);
        assertEquals(FoodStatus.OFFERED, created.getStatus());

        FoodStatus last = null;
        for (int i = 0; i < 50 && last != FoodStatus.UNMATCHED; i++) {
            Thread.sleep(200);
            last = service.getAllPosts().stream().filter(p -> p.getId().equals(created.getId()))
                    .findFirst().orElseThrow().getStatus();
        }
        assertEquals(FoodStatus.UNMATCHED, last);
    }
}
