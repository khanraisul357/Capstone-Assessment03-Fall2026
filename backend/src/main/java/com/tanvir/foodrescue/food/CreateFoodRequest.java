package com.tanvir.foodrescue.food;

public class CreateFoodRequest {
    private String restaurantName;
    private String rawDescription;
    private String readyAt; // optional "HH:mm" (24h). Defaults to the current time.

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }
    public String getReadyAt() { return readyAt; }
    public void setReadyAt(String readyAt) { this.readyAt = readyAt; }
}
