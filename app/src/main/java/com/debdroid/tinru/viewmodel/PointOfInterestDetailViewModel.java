package com.debdroid.tinru.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi.GooglePlacesCustomPlaceDetailResponse;
import com.debdroid.tinru.repository.TinruRepository;

import javax.inject.Inject;

import timber.log.Timber;

public class PointOfInterestDetailViewModel extends ViewModel {
    private LiveData<GooglePlacesCustomPlaceDetailResponse> googlePlacesCustomPlaceDetail;
    private TinruRepository tinruRepository;
    private String currentPlaceId;
    private String previousPlaceId;
    private boolean needFreshData = false;

    @Inject
    public PointOfInterestDetailViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<GooglePlacesCustomPlaceDetailResponse> getPointOfInterestDetail(
            String placeId, String apiKey) {
        Timber.d("getPointOfInterestDetail is called");
        currentPlaceId = placeId;

        // When place id changes, set the googlePlacesCustomPlaceDetail
        // to Null to force data load for new place id
        if (!currentPlaceId.equals(previousPlaceId)) {
            googlePlacesCustomPlaceDetail = null;
            previousPlaceId = currentPlaceId;
            needFreshData = true;
        } else {
            needFreshData = false;
        }

        if (googlePlacesCustomPlaceDetail == null) {
            googlePlacesCustomPlaceDetail = tinruRepository
                    .getGooglePlaceCustomPlaceData(placeId, apiKey, needFreshData);
        }
        return googlePlacesCustomPlaceDetail;
    }
}
