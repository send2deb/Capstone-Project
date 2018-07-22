
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AmadeusSandboxLowFareSearchResponse {

    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("results")
    @Expose
    private List<Result> results = null;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

}
