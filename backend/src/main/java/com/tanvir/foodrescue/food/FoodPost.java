package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.shelter.Shelter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private int readyAtMinutes;        // food is ready for collection from this time
    private int holdUntilMinutes;      // food is no longer safe after this time
    private String pickupNote;

    @Enumerated(EnumType.STRING)
    private FoodStatus status = FoodStatus.UNMATCHED;

    /** Optimistic lock: two simultaneous actions on one batch cannot both win. */
    @Version
    private Long version;

    /** The ONE shelter this batch is currently offered to / accepted by. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_shelter_id")
    private Shelter assignedTo;

    /** Shelters that already declined, timed out or did not show up for THIS batch. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_post_excluded_shelters", joinColumns = @JoinColumn(name = "food_post_id"))
    @Column(name = "shelter_id")
    private Set<Long> excludedShelterIds = new HashSet<>();

    private LocalDateTime postedAt;
    private LocalDateTime offerExpiresAt;
    private LocalDateTime acceptedAt;
    private Integer etaMinutes;

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
    public int getReadyAtMinutes() { return readyAtMinutes; }
    public void setReadyAtMinutes(int readyAtMinutes) { this.readyAtMinutes = readyAtMinutes; }
    public int getHoldUntilMinutes() { return holdUntilMinutes; }
    public void setHoldUntilMinutes(int holdUntilMinutes) { this.holdUntilMinutes = holdUntilMinutes; }
    public String getPickupNote() { return pickupNote; }
    public void setPickupNote(String pickupNote) { this.pickupNote = pickupNote; }
    public FoodStatus getStatus() { return status; }
    public void setStatus(FoodStatus status) { this.status = status; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Shelter getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Shelter assignedTo) { this.assignedTo = assignedTo; }
    public Set<Long> getExcludedShelterIds() { return excludedShelterIds; }
    public void setExcludedShelterIds(Set<Long> excludedShelterIds) { this.excludedShelterIds = excludedShelterIds; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getOfferExpiresAt() { return offerExpiresAt; }
    public void setOfferExpiresAt(LocalDateTime offerExpiresAt) { this.offerExpiresAt = offerExpiresAt; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
}
