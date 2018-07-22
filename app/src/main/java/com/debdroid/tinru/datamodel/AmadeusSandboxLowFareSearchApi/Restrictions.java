
package com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Restrictions {

    @SerializedName("refundable")
    @Expose
    private Boolean refundable;
    @SerializedName("change_penalties")
    @Expose
    private Boolean changePenalties;

    public Boolean getRefundable() {
        return refundable;
    }

    public void setRefundable(Boolean refundable) {
        this.refundable = refundable;
    }

    public Boolean getChangePenalties() {
        return changePenalties;
    }

    public void setChangePenalties(Boolean changePenalties) {
        this.changePenalties = changePenalties;
    }

}
