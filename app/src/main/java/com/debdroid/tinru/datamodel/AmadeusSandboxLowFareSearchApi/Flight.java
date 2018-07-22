
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Flight {

    @SerializedName("departs_at")
    @Expose
    private String departsAt;
    @SerializedName("arrives_at")
    @Expose
    private String arrivesAt;
    @SerializedName("origin")
    @Expose
    private Origin origin;
    @SerializedName("destination")
    @Expose
    private Destination destination;
    @SerializedName("marketing_airline")
    @Expose
    private String marketingAirline;
    @SerializedName("operating_airline")
    @Expose
    private String operatingAirline;
    @SerializedName("flight_number")
    @Expose
    private String flightNumber;
    @SerializedName("aircraft")
    @Expose
    private String aircraft;
    @SerializedName("booking_info")
    @Expose
    private BookingInfo bookingInfo;

    public String getDepartsAt() {
        return departsAt;
    }

    public void setDepartsAt(String departsAt) {
        this.departsAt = departsAt;
    }

    public String getArrivesAt() {
        return arrivesAt;
    }

    public void setArrivesAt(String arrivesAt) {
        this.arrivesAt = arrivesAt;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getMarketingAirline() {
        return marketingAirline;
    }

    public void setMarketingAirline(String marketingAirline) {
        this.marketingAirline = marketingAirline;
    }

    public String getOperatingAirline() {
        return operatingAirline;
    }

    public void setOperatingAirline(String operatingAirline) {
        this.operatingAirline = operatingAirline;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAircraft() {
        return aircraft;
    }

    public void setAircraft(String aircraft) {
        this.aircraft = aircraft;
    }

    public BookingInfo getBookingInfo() {
        return bookingInfo;
    }

    public void setBookingInfo(BookingInfo bookingInfo) {
        this.bookingInfo = bookingInfo;
    }

}
