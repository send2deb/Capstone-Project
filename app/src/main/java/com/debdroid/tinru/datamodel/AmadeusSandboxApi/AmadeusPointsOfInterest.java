package com.debdroid.tinru.datamodel.AmadeusSandboxApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AmadeusPointsOfInterest {
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("categories")
    @Expose
    private List<String> categories = null;
    @SerializedName("amadeus_grades")
    @Expose
    private AmadeusGrades amadeusGrades;
    @SerializedName("geoname_id")
    @Expose
    private Integer geonameId;
    @SerializedName("main_image")
    @Expose
    private String mainImage;
    @SerializedName("amadeus_interest_details")
    @Expose
    private AmadeusInterestDetails amadeusInterestDetails;
    @SerializedName("amadeus_interest_location")
    @Expose
    private AmadeusInterestLocation amadeusInterestLocation;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public AmadeusGrades getAmadeusGrades() {
        return amadeusGrades;
    }

    public void setAmadeusGrades(AmadeusGrades amadeusGrades) {
        this.amadeusGrades = amadeusGrades;
    }

    public Integer getGeonameId() {
        return geonameId;
    }

    public void setGeonameId(Integer geonameId) {
        this.geonameId = geonameId;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public AmadeusInterestDetails getAmadeusInterestDetails() {
        return amadeusInterestDetails;
    }

    public void setAmadeusInterestDetails(AmadeusInterestDetails amadeusInterestDetails) {
        this.amadeusInterestDetails = amadeusInterestDetails;
    }

    public AmadeusInterestLocation getAmadeusInterestLocation() {
        return amadeusInterestLocation;
    }

    public void setAmadeusInterestLocation(AmadeusInterestLocation amadeusInterestLocation) {
        this.amadeusInterestLocation = amadeusInterestLocation;
    }
}
