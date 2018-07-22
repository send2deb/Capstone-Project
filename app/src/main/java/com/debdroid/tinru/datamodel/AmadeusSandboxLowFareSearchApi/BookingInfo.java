
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BookingInfo {

    @SerializedName("travel_class")
    @Expose
    private String travelClass;
    @SerializedName("booking_code")
    @Expose
    private String bookingCode;
    @SerializedName("seats_remaining")
    @Expose
    private Integer seatsRemaining;

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public Integer getSeatsRemaining() {
        return seatsRemaining;
    }

    public void setSeatsRemaining(Integer seatsRemaining) {
        this.seatsRemaining = seatsRemaining;
    }

}
