package com.debdroid.tinru.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import com.debdroid.tinru.dagger.TinruCustomScope;
import com.debdroid.tinru.database.NearbyResultDao;
import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.datamodel.AmadeusPointsOfInterestResponse;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyResponse;
import com.debdroid.tinru.datamodel.Result;
import com.debdroid.tinru.retrofit.AmadeusSandboxPointOfInterestApiService;
import com.debdroid.tinru.retrofit.GooglePlacesNearbySearchApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@TinruCustomScope.TinruApplicationScope //Let Dagger know that this class should be constructed only once
public class TinruRepository {
    private AmadeusSandboxPointOfInterestApiService amadeusSandboxPointOfInterestApiService;
    private GooglePlacesNearbySearchApiService googlePlacesNearbySearchApiService;
    private NearbyResultDao nearbyResultDao;
    private MutableLiveData<List<Result>> nearbyData = new MutableLiveData<List<Result>>();
//    private static boolean isFirstTimeDataLoad = true;

    @Inject
    public TinruRepository(AmadeusSandboxPointOfInterestApiService amadeusSandboxPointOfInterestApiService,
                           GooglePlacesNearbySearchApiService googlePlacesNearbySearchApiService,
                           NearbyResultDao nearbyResultDao) {
        this.amadeusSandboxPointOfInterestApiService = amadeusSandboxPointOfInterestApiService;
        this.googlePlacesNearbySearchApiService = googlePlacesNearbySearchApiService;
        this.nearbyResultDao = nearbyResultDao;
    }

    public LiveData<List<NearbyResultEntity>> getResult(String latLng, int radius, String type,
                                                        String apiKey, boolean needFreshData) {
        Timber.d("getResult is called");
        // Load the data only when ViewModel need it
        if(needFreshData) {
            loadGooglePlaceNearbyData(latLng, radius, type, apiKey);
        }
        // Return current data from the database
        return nearbyResultDao.loadAllNearbyResultEntityAsLiveData();
    }

    private void loadGooglePlaceNearbyData(String latLng, int radius, String type, String apiKey) {
        Timber.d("loadGooglePlaceNearbyData is called");
        Call<GooglePlacesNearbyResponse> googlePlacesNearbyResponseCall =
                googlePlacesNearbySearchApiService.getGooglePlacesNearbyResponse(latLng,radius,type,apiKey);
        googlePlacesNearbyResponseCall.enqueue(new Callback<GooglePlacesNearbyResponse>() {
            @Override
            public void onResponse(Call<GooglePlacesNearbyResponse> call, Response<GooglePlacesNearbyResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Google place nearby api call successful ->" + response.toString());
//                    //use postValue since it is running on background thread.
//                    nearbyData.postValue(response.body().getResults());
                    // First delete all records
                    deleteDataFromNearbyResultTable();
                    // Now insert the new records
                    insertDataToNearbyResultTable(response.body().getResults());
                } else {
                    Timber.e("Google place nearby api response not successful -> "
                            + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesNearbyResponse> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
            }
        });
    }

    private void insertDataToNearbyResultTable(final List<Result> resultList) { // Room does not allow operation on main thread
        Timber.d("insertDataToNearbyResultTable is called");
        @SuppressLint("StaticFieldLeak")
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if(resultList != null) {
                    List<NearbyResultEntity> nearbyResultEntities = new ArrayList<>();
                    for(Result result : resultList) {
                        NearbyResultEntity nearbyResultEntity = new NearbyResultEntity();
                        nearbyResultEntity.id = result.getId();
                        nearbyResultEntity.nearbyName = result.getName();
                        nearbyResultEntity.rating = result.getRating();
                        nearbyResultEntity.vicinity = result.getVicinity();
                        if(result.getOpeningHours() != null) {
                            if (result.getOpeningHours().getOpenNow()) {
                                nearbyResultEntity.openStatus = "Open Now";
                            } else {
                                nearbyResultEntity.openStatus = "Closed Now";
                            }
                        } else {
                            nearbyResultEntity.openStatus = "Open / Close data not available";
                        }
                        if(result.getPhotos() != null) {
                            nearbyResultEntity.photoReference = result.getPhotos().get(0).getPhotoReference();
                        }
                        nearbyResultEntities.add(nearbyResultEntity);
                    }

                    nearbyResultDao.insertBulkResults(nearbyResultEntities);
//                    isFirstTimeDataLoad = false; // Ensure load happens only once
                } else {
                    Timber.e("Json response is null");
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    private void deleteDataFromNearbyResultTable() {
        Timber.d("deleteDataFromNearbyResultTable is called");
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                nearbyResultDao.deleteAllResults();
                return null;
            }
        };
        asyncTask.execute();
    }

    public void getPointsOfInterest() {
        String city = "London";
        String key = "";
        Call<AmadeusPointsOfInterestResponse> amadeusPointsOfInterestResponseCall =
                amadeusSandboxPointOfInterestApiService.getAmadeusPointsOfInterestResponse(city,key);
        amadeusPointsOfInterestResponseCall.enqueue(new Callback<AmadeusPointsOfInterestResponse>() {
            @Override
            public void onResponse(Call<AmadeusPointsOfInterestResponse> call,
                                   Response<AmadeusPointsOfInterestResponse> response) {
                if (response.isSuccessful()) {
                    Timber.d("Amadeus api call successful ->" + response.toString());
                    Timber.d("Data ->" + response.raw().toString());
//                    Timber.d("City name ->" + response.body().toString());
                    Timber.d("City name ->" + response.body().getAmadeusPointsOfInterest().get(0).getTitle());
//                    Timber.d("City lat ->" + response.body().getAmadeusCurrentCity().getAmadeusCityLocationLatLong().getLatitude());
//                    Timber.d("City long ->" + response.body().getAmadeusCurrentCity().getAmadeusCityLocationLatLong().getLongitude());
                } else {
                    Timber.e("Amadeus points of interest api response not successful -> "
                            + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<AmadeusPointsOfInterestResponse> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
            }
        });
    }
}
