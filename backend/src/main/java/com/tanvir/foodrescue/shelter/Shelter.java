package com.tanvir.foodrescue.shelter;

import com.tanvir.foodrescue.util.TimeUtil;
import jakarta.persistence.*;

@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String area;
    private String transportType;      // VAN or RICKSHAW
    private boolean canTakeTrays;      // rickshaw shelters cannot carry trays
    private int maxPortions;           // capacity per batch
    private int latestPickupMinutes;   // collects only before this time, e.g. 22:00 -> 1320
    private int travelMinutes;         // typical trip time to the restaurant
    private int totalClaims;           // times they said yes
    private int completedPickups;      // times they really turned up
    private int noShows;               // times they said yes and did not turn up

    public Shelter() {}

    public Shelter(String name, String area, String transportType, boolean canTakeTrays,
                   int maxPortions, int latestPickupMinutes, int travelMinutes,
                   int totalClaims, int completedPickups, int noShows) {
        this.name = name;
        this.area = area;
        this.transportType = transportType;
        this.canTakeTrays = canTakeTrays;
        this.maxPortions = maxPortions;
        this.latestPickupMinutes = latestPickupMinutes;
        this.travelMinutes = travelMinutes;
        this.totalClaims = totalClaims;
        this.completedPickups = completedPickups;
        this.noShows = noShows;
    }

    /** Share of accepted jobs they actually collected. New shelters start at a neutral 90. */
    public int getReliabilityScore() {
        int finished = completedPickups + noShows;
        if (finished == 0) return 90;
        return (int) Math.round(100.0 * completedPickups / finished);
    }

    public String getLatestPickupFormatted() {
        return TimeUtil.format(latestPickupMinutes);
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
    public int getLatestPickupMinutes() { return latestPickupMinutes; }
    public void setLatestPickupMinutes(int latestPickupMinutes) { this.latestPickupMinutes = latestPickupMinutes; }
    public int getTravelMinutes() { return travelMinutes; }
    public void setTravelMinutes(int travelMinutes) { this.travelMinutes = travelMinutes; }
    public int getTotalClaims() { return totalClaims; }
    public void setTotalClaims(int totalClaims) { this.totalClaims = totalClaims; }
    public int getCompletedPickups() { return completedPickups; }
    public void setCompletedPickups(int completedPickups) { this.completedPickups = completedPickups; }
    public int getNoShows() { return noShows; }
    public void setNoShows(int noShows) { this.noShows = noShows; }
}
