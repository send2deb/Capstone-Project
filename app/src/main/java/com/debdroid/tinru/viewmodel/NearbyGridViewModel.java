package com.debdroid.tinru.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.repository.TinruRepository;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class NearbyGridViewModel extends ViewModel {
    private LiveData<List<NearbyResultEntity>> nearbyResultEntityList;
    private TinruRepository tinruRepository;
    private String currentType;
    private String previousType;
    private String currentLocation;
    private String previousLocation;
    private boolean needFreshData = false;

    @Inject
    public NearbyGridViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<List<NearbyResultEntity>> getResultList(String location, String latLng, int radius, String type, String apiKey) {
        Timber.d("getResultList is called");
        currentType = type;
        currentLocation = location;

        Timber.d("Previous type & location -> " + previousType + " & " + previousLocation);
        Timber.d("Current type & location -> " + currentType + " & " + currentLocation);

        // When type changes, set the nearbyResultEntityList to Null to force data load for new type
        if(!currentType.equals(previousType) || !currentLocation.equals(previousLocation)) {
            Timber.d("Current type and location NOT equal to previous type and location");
            nearbyResultEntityList = null;
            previousType = currentType;
            previousLocation = currentLocation;
            needFreshData = true;
        } else {
            Timber.d("Current type and location equal to previous type and location");
            needFreshData = false;
        }

        if(nearbyResultEntityList == null) {
            Timber.d("nearbyResultEntityList is null");
            nearbyResultEntityList = tinruRepository.getNearbyResult(latLng,radius,type,apiKey, needFreshData);
        }
        return nearbyResultEntityList;
    }
}
