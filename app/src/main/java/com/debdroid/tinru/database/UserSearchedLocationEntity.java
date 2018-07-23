package com.debdroid.tinru.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Entity(tableName = "user_searched_location")
public class UserSearchedLocationEntity {

    @PrimaryKey
    @ColumnInfo(name = "city_name")
    public @NotNull String cityName;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "airport_code")
    public String airportCode;

    @ColumnInfo(name = "datetimestamp")
    public Date datetimestamp;


    public UserSearchedLocationEntity() {}

    public UserSearchedLocationEntity(@NotNull String cityName, String airportCode, double latitude,
                                      double longitude, Date datetimestamp) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.airportCode = airportCode;
        this.datetimestamp = datetimestamp;
    }
}
