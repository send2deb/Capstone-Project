
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fare {

    @SerializedName("total_price")
    @Expose
    private String totalPrice;
    @SerializedName("price_per_adult")
    @Expose
    private PricePerAdult pricePerAdult;
    @SerializedName("restrictions")
    @Expose
    private Restrictions restrictions;

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PricePerAdult getPricePerAdult() {
        return pricePerAdult;
    }

    public void setPricePerAdult(PricePerAdult pricePerAdult) {
        this.pricePerAdult = pricePerAdult;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

}
