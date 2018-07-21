package com.debdroid.tinru.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface NearbyResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleNearbyResultEntity(NearbyResultEntity nearbyResultEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulkNearbyResultEntities(List<NearbyResultEntity> nearbyResultEntities);

    @Delete
    int deleteNearbyResultEntities(List<NearbyResultEntity> nearbyResultEntities);

    @Query("SELECT * FROM nearby_result")
    List<NearbyResultEntity> loadAllNearbyResultEntities();

    @Query("SELECT * FROM nearby_result WHERE id = :id")
    NearbyResultEntity loadSingleNearbyResultEntity(String id);

    @Query("SELECT * FROM nearby_result")
    LiveData<List<NearbyResultEntity>> loadAllNearbyResultEntitiesAsLiveData();

    @Query("SELECT * FROM nearby_result WHERE id = :id")
    LiveData<NearbyResultEntity> loadSingleNearbyResultEntitiesAsLiveData(String id);

    @Query("DELETE from nearby_result")
    void deleteAllNearbyResultEntities();
}
