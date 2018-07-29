package com.debdroid.tinru.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi.AmadeusSandboxLowFareSearchResponse;
import com.debdroid.tinru.repository.TinruRepository;

import javax.inject.Inject;

import timber.log.Timber;

public class FlightListViewModel extends ViewModel {
    private LiveData<AmadeusSandboxLowFareSearchResponse> amadeusSandboxLowFareSearchResponseLiveData;
    private TinruRepository tinruRepository;
    private String currentOrigin;
    private String currentDestination;
    private String previousOrigin;
    private String previousDestination;
    private boolean needFreshData = false;

    @Inject
    public FlightListViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<AmadeusSandboxLowFareSearchResponse> getFlightData(String origin,
                                                                       String destination, boolean nonStop, String departureDate, String apiKey) {
        Timber.d("getFlightData is called");
        currentOrigin = origin;
        currentDestination = destination;

        // When latitude or longitude changes, set the airportCode
        // to Null to force data load for new latitude and longitude
        if ((currentOrigin != previousOrigin) || (currentDestination != previousDestination)) {
            amadeusSandboxLowFareSearchResponseLiveData = null;
            previousOrigin = currentOrigin;
            previousDestination = currentDestination;
            needFreshData = true;
        } else {
            needFreshData = false;
        }

        if (amadeusSandboxLowFareSearchResponseLiveData == null) {
            amadeusSandboxLowFareSearchResponseLiveData = tinruRepository
                    .getLowFareFlights(origin, destination, nonStop, departureDate, apiKey, needFreshData);
        }
        return amadeusSandboxLowFareSearchResponseLiveData;
    }
}
