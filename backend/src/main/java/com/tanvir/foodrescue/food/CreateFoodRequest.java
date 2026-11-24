package com.tanvir.foodrescue.food;

public class CreateFoodRequest {
    private String restaurantName;
    private String rawDescription;

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }
}
