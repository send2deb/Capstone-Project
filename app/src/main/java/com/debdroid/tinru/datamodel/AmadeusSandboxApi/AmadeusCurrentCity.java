package com.debdroid.tinru.datamodel.AmadeusSandboxApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AmadeusCurrentCity {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("amadeus_city_location_lat_long")
    @Expose
    private AmadeusCityLocationLatLong amadeusCityLocationLatLong;
    @SerializedName("geoname_id")
    @Expose
    private Integer geonameId;
    @SerializedName("total_points_of_interest")
    @Expose
    private Integer totalPointsOfInterest;
    @SerializedName("important_note")
    @Expose
    private String importantNote;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AmadeusCityLocationLatLong getAmadeusCityLocationLatLong() {
        return amadeusCityLocationLatLong;
    }

    public void setAmadeusCityLocationLatLong(AmadeusCityLocationLatLong amadeusCityLocationLatLong) {
        this.amadeusCityLocationLatLong = amadeusCityLocationLatLong;
    }

    public Integer getGeonameId() {
        return geonameId;
    }

    public void setGeonameId(Integer geonameId) {
        this.geonameId = geonameId;
    }

    public Integer getTotalPointsOfInterest() {
        return totalPointsOfInterest;
    }

    public void setTotalPointsOfInterest(Integer totalPointsOfInterest) {
        this.totalPointsOfInterest = totalPointsOfInterest;
    }

    public String getImportantNote() {
        return importantNote;
    }

    public void setImportantNote(String importantNote) {
        this.importantNote = importantNote;
    }
}
