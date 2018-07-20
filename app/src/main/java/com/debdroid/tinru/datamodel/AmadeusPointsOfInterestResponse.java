package com.debdroid.tinru.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AmadeusPointsOfInterestResponse {
    @SerializedName("amadeus_current_city")
    @Expose
    private String amadeusCurrentCity;
//    private AmadeusCurrentCity amadeusCurrentCity;
    @SerializedName("amadeus_points_of_interest")
    @Expose
    private List<AmadeusPointsOfInterest> amadeusPointsOfInterest = null;

//    public AmadeusCurrentCity getAmadeusCurrentCity() {
//        return amadeusCurrentCity;
//    }
//
//    public void setAmadeusCurrentCity(AmadeusCurrentCity amadeusCurrentCity) {
//        this.amadeusCurrentCity = amadeusCurrentCity;
//    }

    public List<AmadeusPointsOfInterest> getAmadeusPointsOfInterest() {
        return amadeusPointsOfInterest;
    }

    public void setPointsOfInterest(List<AmadeusPointsOfInterest> amadeusPointsOfInterest) {
        this.amadeusPointsOfInterest = amadeusPointsOfInterest;
    }
}
