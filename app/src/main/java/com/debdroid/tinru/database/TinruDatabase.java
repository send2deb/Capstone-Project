package com.debdroid.tinru.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(version = 1, entities = {UserSearchedLocationEntity.class, NearbyResultEntity.class})
public abstract class TinruDatabase extends RoomDatabase {
    //Zero argument UserSearchedLocation @Dao abstract method
    public abstract UserSearchedLocationDao getUserSearchedLocationDao();

    //Zero argument NearbyResult @Dao abstract method
    public abstract NearbyResultDao getNearbyResultDao();
}
