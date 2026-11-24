package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.shelter.Shelter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_posts")
public class FoodPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String restaurantName;

    @Column(length = 1000)
    private String rawDescription;

    private int estimatedPortions;
    private boolean hasTrays;
    private String holdUntilText;
    private int holdUntilMinutesFromMidnight;
    private String pickupNote;

    @Enumerated(EnumType.STRING)
    private FoodStatus status = FoodStatus.OPEN;

    @Version
    private Long version; // Prevents concurrent double-booking race conditions

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claimed_by_shelter_id")
    private Shelter claimedBy;

    private LocalDateTime postedAt;
    private LocalDateTime claimedAt;

    public FoodPost() {}

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
    public int getHoldUntilMinutesFromMidnight() { return holdUntilMinutesFromMidnight; }
    public void setHoldUntilMinutesFromMidnight(int holdUntilMinutesFromMidnight) {
        this.holdUntilMinutesFromMidnight = holdUntilMinutesFromMidnight;
    }
    public String getPickupNote() { return pickupNote; }
    public void setPickupNote(String pickupNote) { this.pickupNote = pickupNote; }
    public FoodStatus getStatus() { return status; }
    public void setStatus(FoodStatus status) { this.status = status; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Shelter getClaimedBy() { return claimedBy; }
    public void setClaimedBy(Shelter claimedBy) { this.claimedBy = claimedBy; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
}
