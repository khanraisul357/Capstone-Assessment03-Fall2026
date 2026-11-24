package com.tanvir.foodrescue;

import com.tanvir.foodrescue.food.*;
import com.tanvir.foodrescue.seed.DataSeeder;
import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flowtest;DB_CLOSE_DELAY=-1",
        "app.expiry-check-ms=3600000"
})
class FoodRescueFlowTest {

    static final String SAMPLE =
            "about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate";

    @Autowired FoodRescueService service;
    @Autowired FoodPostRepository posts;
    @Autowired ShelterRepository shelters;
    @Autowired DataSeeder seeder;
    @Autowired QuickFoodParser parser;

    @BeforeEach
    void reset() {
        posts.deleteAll();
        shelters.deleteAll();
        seeder.run();
    }

    Long id(String nameStart) {
        return shelters.findAll().stream().filter(s -> s.getName().startsWith(nameStart))
                .map(Shelter::getId).findFirst().orElseThrow();
    }

    FoodPostResponseDTO post(String text, String readyAt) {
        CreateFoodRequest r = new CreateFoodRequest();
        r.setRawDescription(text);
        r.setReadyAt(readyAt);
        return service.createPost(r);
    }

    ShelterMatchDTO match(FoodPostResponseDTO p, String nameStart) {
        return p.getShelterMatches().stream().filter(m -> m.getShelterName().startsWith(nameStart))
                .findFirst().orElseThrow();
    }

    @Test
    void parserUnderstandsTanvirsSentence() {
        QuickFoodParser.ParsedFood f = parser.parse(SAMPLE);
        assertEquals(60, f.estimatedPortions);          // 30 plates + 2 trays * 15
        assertTrue(f.hasTrays);
        assertEquals(23 * 60 + 30, f.holdUntilMinutes);
        assertEquals("Back gate", f.pickupNote);
    }

    @Test
    void parserTreatsNoTraysAsNoTrays() {
        QuickFoodParser.ParsedFood f = parser.parse("25 packs vegetable khichuri, no trays, hold until 11:00pm, front counter");
        assertFalse(f.hasTrays);
        assertEquals(25, f.estimatedPortions);
        assertEquals(23 * 60, f.holdUntilMinutes);
    }

    @Test
    void offersToExactlyOneBestEligibleShelter() {
        FoodPostResponseDTO p = post(SAMPLE, "21:45");
        assertEquals(FoodStatus.OFFERED, p.getStatus());
        assertEquals("Alo Community Kitchen", p.getAssignedShelterName());

        assertFalse(match(p, "Shobuj").isEligible(), "rickshaw cannot take trays");
        assertFalse(match(p, "Early Night").isEligible(), "would arrive after its 10 PM cutoff");
        assertFalse(match(p, "Green Crescent").isEligible(), "no trays and only 40 portions");
        assertTrue(match(p, "Metro").isEligible(), "unreliable but able, so it stays as backup");
        assertTrue(match(p, "Alo").getMatchScore() > match(p, "Metro").getMatchScore());
    }

    @Test
    void shelterThatIsNotOfferedCannotClaim() {
        FoodPostResponseDTO p = post(SAMPLE, "21:45");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.acceptOffer(p.getId(), id("Metro")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void declineMovesOfferToNextShelterThenToUnmatched() {
        FoodPostResponseDTO p = post(SAMPLE, "21:45");
        FoodPostResponseDTO afterDecline = service.declineOffer(p.getId(), id("Alo"));
        assertEquals(FoodStatus.OFFERED, afterDecline.getStatus());
        assertEquals("Metro Night Relief", afterDecline.getAssignedShelterName());

        FoodPostResponseDTO none = service.declineOffer(p.getId(), id("Metro"));
        assertEquals(FoodStatus.UNMATCHED, none.getStatus());
        assertNull(none.getAssignedShelterId());
    }

    @Test
    void acceptThenPickupRaisesReliability() {
        FoodPostResponseDTO p = post(SAMPLE, "21:45");
        FoodPostResponseDTO accepted = service.acceptOffer(p.getId(), id("Alo"));
        assertEquals(FoodStatus.ACCEPTED, accepted.getStatus());
        assertEquals("10:05 PM", accepted.getEtaText());   // 21:45 + 20 min

        FoodPostResponseDTO done = service.markPickedUp(p.getId());
        assertEquals(FoodStatus.PICKED_UP, done.getStatus());
        assertEquals(13, shelters.findById(id("Alo")).orElseThrow().getCompletedPickups());
    }

    @Test
    void noShowPenalisesShelterAndReOffersToNextWithoutReusingIt() {
        FoodPostResponseDTO p = post(SAMPLE, "21:45");
        service.acceptOffer(p.getId(), id("Alo"));
        int before = shelters.findById(id("Alo")).orElseThrow().getNoShows();

        FoodPostResponseDTO re = service.reportNoShow(p.getId());
        assertEquals(before + 1, shelters.findById(id("Alo")).orElseThrow().getNoShows());
        assertEquals(FoodStatus.OFFERED, re.getStatus());
        assertEquals("Metro Night Relief", re.getAssignedShelterName());
        assertFalse(match(re, "Alo").isEligible(), "no-show shelter is excluded for this batch");
    }

    @Test
    void smallNoTrayBatchGoesToNearbyRickshawShelter() {
        FoodPostResponseDTO p = post("25 packs vegetable khichuri, no trays, hold until 11:00pm, front counter", "21:45");
        assertEquals("Green Crescent Home", p.getAssignedShelterName());
    }

    @Test
    void earlierReadyTimeMakesEarlyCutoffShelterEligible() {
        FoodPostResponseDTO p = post(SAMPLE, "21:00");   // 21:00 + 25 min = 21:25 < 22:00
        assertTrue(match(p, "Early Night").isEligible());
    }

    @Test
    void foodThatSpoilsBeforeAnyShelterCanArriveIsUnmatched() {
        FoodPostResponseDTO p = post("30 plates rice, hold until 10:00pm, back gate", "21:55");
        assertEquals(FoodStatus.UNMATCHED, p.getStatus());
    }

    @Test
    void emptyDescriptionIsRejected() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> post("  ", null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
