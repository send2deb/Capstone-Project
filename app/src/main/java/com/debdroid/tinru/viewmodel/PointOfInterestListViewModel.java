package com.debdroid.tinru.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.debdroid.tinru.database.PointOfInterestResultEntity;
import com.debdroid.tinru.repository.TinruRepository;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class PointOfInterestListViewModel extends ViewModel {
    private LiveData<List<PointOfInterestResultEntity>> pointOfInterestResultEntityList;
    private TinruRepository tinruRepository;
    private String currentLocationPointOfInterest;
    private String previousLocationPointOfInterest;
    private boolean needFreshData = false;

    @Inject
    public PointOfInterestListViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<List<PointOfInterestResultEntity>> getPointOfInterestResultList(
            String locationPointOfInterest, String apiKey) {
        Timber.d("getPointOfInterestResultList is called");
        currentLocationPointOfInterest = locationPointOfInterest;

        Timber.d("Previous location point of interest -> " + previousLocationPointOfInterest);
        Timber.d("Current location point of interest -> " + currentLocationPointOfInterest);

        // When location point of interest changes, set the pointOfInterestResultEntityList to Null
        // to force data load for new location point of interest
        if(!currentLocationPointOfInterest.equals(previousLocationPointOfInterest)) {
            Timber.d("Current location point of interest '" + currentLocationPointOfInterest +
                    "' not equal to previous location point of interest '" + previousLocationPointOfInterest + "'");
            pointOfInterestResultEntityList = null;
            previousLocationPointOfInterest = currentLocationPointOfInterest;
            needFreshData = true;
        } else {
            Timber.d("Current location point of interest '" + currentLocationPointOfInterest +
                    "' is equal to previous location point of interest '" + previousLocationPointOfInterest + "'");
            needFreshData = false;
        }
        if(pointOfInterestResultEntityList == null) {
            Timber.d("pointOfInterestResultEntityList is null");
            pointOfInterestResultEntityList = tinruRepository
                    .getPointOfInterestResult(locationPointOfInterest,apiKey, needFreshData);
        }
        return pointOfInterestResultEntityList;
    }
}