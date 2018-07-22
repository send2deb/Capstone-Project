
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("itineraries")
    @Expose
    private List<Itinerary> itineraries = null;
    @SerializedName("fare")
    @Expose
    private Fare fare;

    public List<Itinerary> getItineraries() {
        return itineraries;
    }

    public void setItineraries(List<Itinerary> itineraries) {
        this.itineraries = itineraries;
    }

    public Fare getFare() {
        return fare;
    }

    public void setFare(Fare fare) {
        this.fare = fare;
    }

}
