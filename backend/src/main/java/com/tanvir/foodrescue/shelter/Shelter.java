package com.tanvir.foodrescue.shelter;

import jakarta.persistence.*;

@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String area;
    private String transportType; // VAN or RICKSHAW
    private boolean canTakeTrays;
    private int maxPortions;
    private int latestPickupMinutesFromMidnight; // e.g., 22:00 -> 1320, 23:59 -> 1439
    private int totalClaims;
    private int completedPickups;
    private int noShows;

    public Shelter() {}

    public Shelter(String name, String area, String transportType, boolean canTakeTrays,
                   int maxPortions, int latestPickupMinutesFromMidnight,
                   int totalClaims, int completedPickups, int noShows) {
        this.name = name;
        this.area = area;
        this.transportType = transportType;
        this.canTakeTrays = canTakeTrays;
        this.maxPortions = maxPortions;
        this.latestPickupMinutesFromMidnight = latestPickupMinutesFromMidnight;
        this.totalClaims = totalClaims;
        this.completedPickups = completedPickups;
        this.noShows = noShows;
    }

    public int getReliabilityScore() {
        if (totalClaims == 0) return 90; // Neutral high trust for new shelters
        double rate = ((double) completedPickups / totalClaims) * 100.0;
        return (int) Math.round(rate);
    }

    public String getLatestPickupFormatted() {
        int h = latestPickupMinutesFromMidnight / 60;
        int m = latestPickupMinutesFromMidnight % 60;
        String ampm = h >= 12 ? "PM" : "AM";
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return String.format("%d:%02d %s", h12, m, ampm);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public boolean isCanTakeTrays() { return canTakeTrays; }
    public void setCanTakeTrays(boolean canTakeTrays) { this.canTakeTrays = canTakeTrays; }
    public int getMaxPortions() { return maxPortions; }
    public void setMaxPortions(int maxPortions) { this.maxPortions = maxPortions; }
    public int getLatestPickupMinutesFromMidnight() { return latestPickupMinutesFromMidnight; }
    public void setLatestPickupMinutesFromMidnight(int latestPickupMinutesFromMidnight) {
        this.latestPickupMinutesFromMidnight = latestPickupMinutesFromMidnight;
    }
    public int getTotalClaims() { return totalClaims; }
    public void setTotalClaims(int totalClaims) { this.totalClaims = totalClaims; }
    public int getCompletedPickups() { return completedPickups; }
    public void setCompletedPickups(int completedPickups) { this.completedPickups = completedPickups; }
    public int getNoShows() { return noShows; }
    public void setNoShows(int noShows) { this.noShows = noShows; }
}
