
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PricePerAdult {

    @SerializedName("total_fare")
    @Expose
    private String totalFare;
    @SerializedName("tax")
    @Expose
    private String tax;

    public String getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(String totalFare) {
        this.totalFare = totalFare;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

}
