package com.tanvir.foodrescue.food;

import java.time.LocalDateTime;
import java.util.List;

public class FoodPostResponseDTO {
    private Long id;
    private String restaurantName;
    private String rawDescription;
    private int estimatedPortions;
    private boolean hasTrays;
    private String readyAtText;
    private String holdUntilText;
    private String pickupNote;
    private FoodStatus status;
    private Long assignedShelterId;
    private String assignedShelterName;
    private Integer assignedShelterReliability;
    private Long offerSecondsLeft;
    private String etaText;
    private LocalDateTime postedAt;
    private List<ShelterMatchDTO> shelterMatches;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }
    public int getEstimatedPortions() { return estimatedPortions; }
    public void setEstimatedPortions(int estimatedPortions) { this.estimatedPortions = estimatedPortions; }
    public boolean isHasTrays() { return hasTrays; }
    public void setHasTrays(boolean hasTrays) { this.hasTrays = hasTrays; }
    public String getReadyAtText() { return readyAtText; }
    public void setReadyAtText(String readyAtText) { this.readyAtText = readyAtText; }
    public String getHoldUntilText() { return holdUntilText; }
    public void setHoldUntilText(String holdUntilText) { this.holdUntilText = holdUntilText; }
    public String getPickupNote() { return pickupNote; }
    public void setPickupNote(String pickupNote) { this.pickupNote = pickupNote; }
    public FoodStatus getStatus() { return status; }
    public void setStatus(FoodStatus status) { this.status = status; }
    public Long getAssignedShelterId() { return assignedShelterId; }
    public void setAssignedShelterId(Long assignedShelterId) { this.assignedShelterId = assignedShelterId; }
    public String getAssignedShelterName() { return assignedShelterName; }
    public void setAssignedShelterName(String assignedShelterName) { this.assignedShelterName = assignedShelterName; }
    public Integer getAssignedShelterReliability() { return assignedShelterReliability; }
    public void setAssignedShelterReliability(Integer assignedShelterReliability) { this.assignedShelterReliability = assignedShelterReliability; }
    public Long getOfferSecondsLeft() { return offerSecondsLeft; }
    public void setOfferSecondsLeft(Long offerSecondsLeft) { this.offerSecondsLeft = offerSecondsLeft; }
    public String getEtaText() { return etaText; }
    public void setEtaText(String etaText) { this.etaText = etaText; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public List<ShelterMatchDTO> getShelterMatches() { return shelterMatches; }
    public void setShelterMatches(List<ShelterMatchDTO> shelterMatches) { this.shelterMatches = shelterMatches; }
}
