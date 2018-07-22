
package com.debdroid.tinru.datamodel.AmadeusSandboxAirportSearchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AmadeusSandboxAirportSearchResponse {

    @SerializedName("airport")
    @Expose
    private String airport;
    @SerializedName("airport_name")
    @Expose
    private String airportName;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("city_name")
    @Expose
    private String cityName;
    @SerializedName("distance")
    @Expose
    private Integer distance;
    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("aircraft_movements")
    @Expose
    private Integer aircraftMovements;
    @SerializedName("timezone")
    @Expose
    private String timezone;

    public String getAirport() {
        return airport;
    }

    public void setAirport(String airport) {
        this.airport = airport;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getAircraftMovements() {
        return aircraftMovements;
    }

    public void setAircraftMovements(Integer aircraftMovements) {
        this.aircraftMovements = aircraftMovements;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

}
