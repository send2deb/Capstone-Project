package com.debdroid.tinru.datamodel.AmadeusSandboxApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AmadeusGrades {
    @SerializedName("city_grade")
    @Expose
    private Integer cityGrade;
    @SerializedName("yapq_grade")
    @Expose
    private Integer yapqGrade;

    public Integer getCityGrade() {
        return cityGrade;
    }

    public void setCityGrade(Integer cityGrade) {
        this.cityGrade = cityGrade;
    }

    public Integer getYapqGrade() {
        return yapqGrade;
    }

    public void setYapqGrade(Integer yapqGrade) {
        this.yapqGrade = yapqGrade;
    }
}
