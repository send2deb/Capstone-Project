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

    @ColumnInfo(name = "datetimestamp")
    public Date datetimestamp;


    public UserSearchedLocationEntity() {}

    public UserSearchedLocationEntity(@NotNull String cityName, double latitude,
                                      double longitude, Date datetimestamp) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetimestamp = datetimestamp;
    }
}
