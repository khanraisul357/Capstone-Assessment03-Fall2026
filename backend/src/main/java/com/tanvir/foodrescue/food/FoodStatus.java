package com.tanvir.foodrescue.food;

public enum FoodStatus {
    UNMATCHED,  // no shelter can collect it in time (restaurant is told early)
    OFFERED,    // offered to exactly ONE shelter, waiting for its answer
    ACCEPTED,   // that shelter said yes, it is on its way (ETA known)
    PICKED_UP   // collected, nothing wasted
}
