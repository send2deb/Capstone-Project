
package com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GooglePlacesCustomPlaceDetailResponse {

    @SerializedName("html_attributions")
    @Expose
    private List<Object> htmlAttributions = null;
    @SerializedName("result")
    @Expose
    private CustomPlaceDetailResult customPlaceDetailResult;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public CustomPlaceDetailResult getCustomPlaceDetailResult() {
        return customPlaceDetailResult;
    }

    public void setCustomPlaceDetailResult(CustomPlaceDetailResult customPlaceDetailResult) {
        this.customPlaceDetailResult = customPlaceDetailResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
