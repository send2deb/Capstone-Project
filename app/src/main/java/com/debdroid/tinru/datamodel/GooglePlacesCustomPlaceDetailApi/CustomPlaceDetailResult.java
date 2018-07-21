
package com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomPlaceDetailResult {

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;
    @SerializedName("reviews")
    @Expose
    private List<Review> reviews = null;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("website")
    @Expose
    private String website;

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
