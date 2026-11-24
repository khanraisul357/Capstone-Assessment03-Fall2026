package com.tanvir.foodrescue.food;

import java.time.LocalDateTime;
import java.util.List;

public class FoodPostResponseDTO {
    private Long id;
    private String restaurantName;
    private String rawDescription;
    private int estimatedPortions;
    private boolean hasTrays;
    private String holdUntilText;
    private String pickupNote;
    private FoodStatus status;
    private Long claimedByShelterId;
    private String claimedByShelterName;
    private Integer claimedByReliability;
    private LocalDateTime postedAt;
    private LocalDateTime claimedAt;
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
    public String getHoldUntilText() { return holdUntilText; }
    public void setHoldUntilText(String holdUntilText) { this.holdUntilText = holdUntilText; }
    public String getPickupNote() { return pickupNote; }
    public void setPickupNote(String pickupNote) { this.pickupNote = pickupNote; }
    public FoodStatus getStatus() { return status; }
    public void setStatus(FoodStatus status) { this.status = status; }
    public Long getClaimedByShelterId() { return claimedByShelterId; }
    public void setClaimedByShelterId(Long claimedByShelterId) { this.claimedByShelterId = claimedByShelterId; }
    public String getClaimedByShelterName() { return claimedByShelterName; }
    public void setClaimedByShelterName(String claimedByShelterName) { this.claimedByShelterName = claimedByShelterName; }
    public Integer getClaimedByReliability() { return claimedByReliability; }
    public void setClaimedByReliability(Integer claimedByReliability) { this.claimedByReliability = claimedByReliability; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public List<ShelterMatchDTO> getShelterMatches() { return shelterMatches; }
    public void setShelterMatches(List<ShelterMatchDTO> shelterMatches) { this.shelterMatches = shelterMatches; }
}
