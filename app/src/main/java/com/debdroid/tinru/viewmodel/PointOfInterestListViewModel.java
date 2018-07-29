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

        // When location point of interest changes, set the pointOfInterestResultEntityList to Null
        // to force data load for new location point of interest
        if (!currentLocationPointOfInterest.equals(previousLocationPointOfInterest)) {
            pointOfInterestResultEntityList = null;
            previousLocationPointOfInterest = currentLocationPointOfInterest;
            needFreshData = true;
        } else {
            needFreshData = false;
        }
        if (pointOfInterestResultEntityList == null) {
            pointOfInterestResultEntityList = tinruRepository
                    .getPointOfInterestResult(locationPointOfInterest, apiKey, needFreshData);
        }
        return pointOfInterestResultEntityList;
    }
}