package com.tanvir.foodrescue.seed;

import com.tanvir.foodrescue.shelter.Shelter;
import com.tanvir.foodrescue.shelter.ShelterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Five shelters that mirror the differences described in the scenario. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ShelterRepository shelterRepository;

    public DataSeeder(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    @Override
    public void run(String... args) {
        if (shelterRepository.count() > 0) return;

        // name, area, transport, canTakeTrays, maxPortions, collects before, travel min, claims, completed, noShows
        shelterRepository.save(new Shelter("Alo Community Kitchen", "Dhanmondi 15", "VAN",
                true, 120, 23 * 60 + 59, 20, 12, 12, 0));        // has a van, very reliable

        shelterRepository.save(new Shelter("Shobuj Youth Shelter", "Jigatola", "RICKSHAW",
                false, 50, 23 * 60 + 30, 25, 10, 9, 1));         // rickshaw: no trays, max ~50 portions

        shelterRepository.save(new Shelter("Early Night Care Home", "Kalabagan", "VAN",
                true, 100, 22 * 60, 25, 8, 8, 0));               // collects anything, but only before 10 PM

        shelterRepository.save(new Shelter("Metro Night Relief", "Mohammadpur", "VAN",
                true, 80, 23 * 60 + 45, 30, 6, 3, 3));           // history of saying yes and not turning up

        shelterRepository.save(new Shelter("Green Crescent Home", "Lalmatia", "RICKSHAW",
                false, 40, 23 * 60, 10, 4, 4, 0));               // close by, small, no trays
    }
}
