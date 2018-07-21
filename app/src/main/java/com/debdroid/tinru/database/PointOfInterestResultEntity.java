package com.debdroid.tinru.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "point_of_interest_result")
public class PointOfInterestResultEntity {
    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "point_of_interest_name")
    public String pointOfInterestName;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "formatted_address")
    public String formattedAddress;

    @ColumnInfo(name = "place_id")
    public String placeId;

    @ColumnInfo(name = "rating")
    public double rating;

    @ColumnInfo(name = "photo_reference")
    public String photoReference;

    public PointOfInterestResultEntity() {}

    public PointOfInterestResultEntity(@NotNull String id, String pointOfInterestName, double latitude, double longitude,
                                       String formattedAddress, String placeId, double rating, String photoReference) {
        this.id = id;
        this.pointOfInterestName = pointOfInterestName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.formattedAddress = formattedAddress;
        this.placeId = placeId;
        this.rating = rating;
        this.photoReference = photoReference;
    }
}
