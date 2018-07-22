package com.debdroid.tinru.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.debdroid.tinru.repository.TinruRepository;

import javax.inject.Inject;

import timber.log.Timber;

public class HomeViewModel extends ViewModel {
    private LiveData<String> airportCode;
    private TinruRepository tinruRepository;
    private double currentlatitude;
    private double currentlongitude;
    private double previouslatitude;
    private double previouslongitude;
    private boolean needFreshData = false;

    @Inject
    public HomeViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<String> getAirportCode(double latitude, double longitude, String apiKey) {
        Timber.d("getAirportCode is called");
        currentlatitude = latitude;
        currentlongitude = longitude;

        Timber.d("Previous latitude & longitude -> " + previouslatitude + " & " + previouslongitude);
        Timber.d("Current latitude & longitude -> " + currentlatitude + " & " + currentlongitude);

        // When latitude or longitude changes, set the airportCode
        // to Null to force data load for new latitude and longitude
        if((currentlatitude != previouslatitude) || (previouslongitude != previouslongitude)) {
            airportCode = null;
            previouslatitude = currentlatitude;
            previouslongitude = previouslongitude;
            needFreshData = true;
        } else {
            needFreshData = false;
        }

        if(airportCode == null) {
            airportCode = tinruRepository
                    .getAirportCode(latitude, longitude, apiKey, needFreshData);
        }
        return airportCode;
    }
}
