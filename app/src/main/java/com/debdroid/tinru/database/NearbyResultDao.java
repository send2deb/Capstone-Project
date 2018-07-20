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
    void insertSingleResult(NearbyResultEntity nearbyResultEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulkResults(List<NearbyResultEntity> nearbyResultEntities);

    @Delete
    int deleteResults(List<NearbyResultEntity> nearbyResultEntities);

    @Query("SELECT * FROM nearby_result")
    List<NearbyResultEntity> loadAllNearbyResultEntity();

    @Query("SELECT * FROM nearby_result WHERE id = :id")
    NearbyResultEntity loadSingleNearbyResultEntity(String id);

    @Query("SELECT * FROM nearby_result")
    LiveData<List<NearbyResultEntity>> loadAllNearbyResultEntityAsLiveData();

    @Query("SELECT * FROM nearby_result WHERE id = :id")
    LiveData<NearbyResultEntity> loadSingleNearbyResultEntityAsLiveData(String id);

    @Query("DELETE from nearby_result")
    void deleteAllResults();
}
