package com.debdroid.tinru.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface UserSearchedLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleLocation(UserSearchedLocationEntity userSearchedLocationEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulkLocations(List<UserSearchedLocationEntity> userSearchedLocationEntities);

    @Delete
    int deleteLocations(List<UserSearchedLocationEntity> userSearchedLocationEntities);

    @Query("SELECT * FROM user_searched_location")
    List<UserSearchedLocationEntity> loadAllUserSearchedLocationEntity();

    @Query("SELECT * FROM user_searched_location ORDER BY datetimestamp DESC")
    List<UserSearchedLocationEntity> loadCustomSearchedLocationEntity();

    @Query("SELECT * FROM user_searched_location")
    LiveData<List<UserSearchedLocationEntity>> loadAllUserSearchedLocationEntityAsLiveData();

    @Query("SELECT * FROM user_searched_location ORDER BY datetimestamp DESC")
    LiveData<UserSearchedLocationEntity> loadCustomUserSearchedLocationEntityAsLiveData();
}
