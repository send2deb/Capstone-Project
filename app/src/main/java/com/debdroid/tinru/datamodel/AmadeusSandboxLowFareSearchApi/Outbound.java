
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Outbound {

    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("flights")
    @Expose
    private List<Flight> flights = null;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

}
