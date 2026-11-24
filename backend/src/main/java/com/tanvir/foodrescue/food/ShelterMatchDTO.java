package com.tanvir.foodrescue.food;

import java.util.List;

public class ShelterMatchDTO {
    private Long shelterId;
    private String shelterName;
    private String area;
    private String transportType;
    private boolean canTakeTrays;
    private int maxPortions;
    private String latestPickupFormatted;
    private int travelMinutes;
    private String arriveBy;
    private int reliabilityScore;
    private int noShows;
    private boolean eligible;
    private int matchScore;
    private List<String> warnings;

    public Long getShelterId() { return shelterId; }
    public void setShelterId(Long shelterId) { this.shelterId = shelterId; }
    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public boolean isCanTakeTrays() { return canTakeTrays; }
    public void setCanTakeTrays(boolean canTakeTrays) { this.canTakeTrays = canTakeTrays; }
    public int getMaxPortions() { return maxPortions; }
    public void setMaxPortions(int maxPortions) { this.maxPortions = maxPortions; }
    public String getLatestPickupFormatted() { return latestPickupFormatted; }
    public void setLatestPickupFormatted(String latestPickupFormatted) { this.latestPickupFormatted = latestPickupFormatted; }
    public int getTravelMinutes() { return travelMinutes; }
    public void setTravelMinutes(int travelMinutes) { this.travelMinutes = travelMinutes; }
    public String getArriveBy() { return arriveBy; }
    public void setArriveBy(String arriveBy) { this.arriveBy = arriveBy; }
    public int getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(int reliabilityScore) { this.reliabilityScore = reliabilityScore; }
    public int getNoShows() { return noShows; }
    public void setNoShows(int noShows) { this.noShows = noShows; }
    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }
    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
