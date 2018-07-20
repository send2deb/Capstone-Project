package com.debdroid.tinru.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "nearby_result")
public class NearbyResultEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "nearby_name")
    public String nearbyName;

    @ColumnInfo(name = "rating")
    public double rating;

    @ColumnInfo(name = "open_Status")
    public String openStatus;

    @ColumnInfo(name = "photo_reference")
    public String photoReference;

    public NearbyResultEntity() {}

    public NearbyResultEntity(int id, String nearbyName, double rating, String openStatus, String photoReference) {
        this.id = id;
        this.nearbyName = nearbyName;
        this.rating = rating;
        this.openStatus = openStatus;
        this.photoReference = photoReference;
    }
}
