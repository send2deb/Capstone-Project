package com.debdroid.tinru.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PointOfInterestResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSinglePointOfInterestResultEntity(PointOfInterestResultEntity pointOfInterestResultEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulkPointOfInterestResultEntities(List<PointOfInterestResultEntity> pointOfInterestResultEntities);

    @Delete
    int deletePointOfInterestResultEntities(List<PointOfInterestResultEntity> pointOfInterestResultEntities);

    @Query("SELECT * FROM point_of_interest_result")
    List<PointOfInterestResultEntity> loadAllPointOfInterestResultEntities();

    @Query("SELECT * FROM point_of_interest_result WHERE id = :id")
    PointOfInterestResultEntity loadSinglePointOfInterestResultEntity(String id);

    @Query("SELECT * FROM point_of_interest_result")
    LiveData<List<PointOfInterestResultEntity>> loadAllPointOfInterestResultEntitiesAsLiveData();

    @Query("SELECT * FROM point_of_interest_result WHERE id = :id")
    LiveData<PointOfInterestResultEntity> loadPointOfInterestResultEntitiesAsLiveData(String id);

    @Query("DELETE from point_of_interest_result")
    void deleteAllPointOfInterestResultEntities();
}
