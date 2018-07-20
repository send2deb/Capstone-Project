package com.debdroid.tinru.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "user_searched_location")
public class UserSearchedLocationEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "city_name")
    public String cityName;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "google_maps_link")
    public String googleMapsLink;

    public UserSearchedLocationEntity() {}

    public UserSearchedLocationEntity(int id, String cityName, double latitude, double longitude, String googleMapsLink) {
        this.id = id;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.googleMapsLink = googleMapsLink;
    }
}
