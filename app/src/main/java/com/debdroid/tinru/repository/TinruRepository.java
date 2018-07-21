package com.debdroid.tinru.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import com.debdroid.tinru.dagger.TinruCustomScope;
import com.debdroid.tinru.database.NearbyResultDao;
import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.database.PointOfInterestResultDao;
import com.debdroid.tinru.database.PointOfInterestResultEntity;
import com.debdroid.tinru.datamodel.AmadeusSandboxApi.AmadeusPointsOfInterestResponse;
import com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi.GooglePlacesCustomPlaceDetailResponse;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.GooglePlacesNearbyResponse;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.Result;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.GooglePlacesTextSearchResponse;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.TextSearchResult;
import com.debdroid.tinru.retrofit.AmadeusSandboxPointOfInterestApiService;
import com.debdroid.tinru.retrofit.GooglePlacesApiService;

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
    private GooglePlacesApiService googlePlacesApiService;
    private NearbyResultDao nearbyResultDao;
    private PointOfInterestResultDao pointOfInterestResultDao;
    private MutableLiveData<GooglePlacesCustomPlaceDetailResponse> googlePlacesCustomPlaceDetail;

    @Inject
    public TinruRepository(AmadeusSandboxPointOfInterestApiService amadeusSandboxPointOfInterestApiService,
                           GooglePlacesApiService googlePlacesApiService, NearbyResultDao nearbyResultDao,
                           PointOfInterestResultDao pointOfInterestResultDao) {
        this.amadeusSandboxPointOfInterestApiService = amadeusSandboxPointOfInterestApiService;
        this.googlePlacesApiService = googlePlacesApiService;
        this.nearbyResultDao = nearbyResultDao;
        this.pointOfInterestResultDao = pointOfInterestResultDao;
    }

    public LiveData<List<NearbyResultEntity>> getNearbyResult(String latLng, int radius, String type,
                                                              String apiKey, boolean needFreshData) {
        Timber.d("getNearbyResult is called");
        // Load the data only when ViewModel need it
        if(needFreshData) {
            loadGooglePlaceNearbyData(latLng, radius, type, apiKey);
        }
        // Return current data from the database
        return nearbyResultDao.loadAllNearbyResultEntitiesAsLiveData();
    }

    public LiveData<List<PointOfInterestResultEntity>> getPointOfInterestResult(
            String locationPointOfInterest, String apiKey, boolean needFreshData) {
        Timber.d("getPointOfInterestResult is called");
        // Load the data only when ViewModel need it
        if(needFreshData) {
            loadGooglePlacePointOfInterestData(locationPointOfInterest, apiKey);
        }
        // Return current data from the database
        return pointOfInterestResultDao.loadAllPointOfInterestResultEntitiesAsLiveData();
    }

    public LiveData<GooglePlacesCustomPlaceDetailResponse> getGooglePlaceCustomPlaceData(
            String placeId, String apiKey,boolean needFreshData) {
        Timber.d("getGooglePlaceCustomPlaceData is called");
        // Load the data only when ViewModel need it
        if(needFreshData) googlePlacesCustomPlaceDetail = null;

        if(googlePlacesCustomPlaceDetail == null) {
            googlePlacesCustomPlaceDetail = new MutableLiveData<>();
            loadGooglePlaceCustomPlaceData(placeId, apiKey);
        }
        // Return current data from the database
        return googlePlacesCustomPlaceDetail;
    }

    private void loadGooglePlaceNearbyData(String latLng, int radius, String type, String apiKey) {
        Timber.d("loadGooglePlaceNearbyData is called");
        Call<GooglePlacesNearbyResponse> googlePlacesNearbyResponseCall =
                googlePlacesApiService.getGooglePlacesNearbyResponse(latLng,radius,type,apiKey);
        googlePlacesNearbyResponseCall.enqueue(new Callback<GooglePlacesNearbyResponse>() {
            @Override
            public void onResponse(Call<GooglePlacesNearbyResponse> call, Response<GooglePlacesNearbyResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Google place nearby api call successful ->" + response.toString());
                    // First delete all records
                    deleteDataFromNearbyResultTable();
                    // Now insert the new records
                    insertDataToNearbyResultTable(response.body().getResults());
                } else {
                    Timber.e("Google place nearby api response not successful -> " + response.raw().toString());
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
                        if(result.getGeometry() != null) {
                            nearbyResultEntity.latitude = result.getGeometry().getLocation().getLat();
                            nearbyResultEntity.longitude = result.getGeometry().getLocation().getLng();
                        } else {
                            nearbyResultEntity.latitude = 0.0;
                            nearbyResultEntity.longitude = 0.0;
                        }
                        nearbyResultEntity.rating = result.getRating();
                        if(result.getOpeningHours() != null) {
                            if (result.getOpeningHours().getOpenNow()) {
                                nearbyResultEntity.openStatus = "Open Now";
                            } else {
                                nearbyResultEntity.openStatus = "Closed Now";
                            }
                        } else {
                            nearbyResultEntity.openStatus = "Open / Close data not available";
                        }
                        nearbyResultEntity.vicinity = result.getVicinity();
                        if(result.getPhotos() != null) {
                            nearbyResultEntity.photoReference = result.getPhotos().get(0).getPhotoReference();
                        }
                        nearbyResultEntities.add(nearbyResultEntity);
                    }

                    nearbyResultDao.insertBulkNearbyResultEntities(nearbyResultEntities);
                } else {
                    Timber.e("Google Place nearby api Json response is null");
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
                nearbyResultDao.deleteAllNearbyResultEntities();
                return null;
            }
        };
        asyncTask.execute();
    }

    private void loadGooglePlacePointOfInterestData(String locationPointOfInterest, String apiKey) {
        Timber.d("loadGooglePlacePointOfInterestData is called");
        Call<GooglePlacesTextSearchResponse> googlePlacesTextSearchResponseCall =
                googlePlacesApiService.getGooglePlacesTextSearchResponse(locationPointOfInterest,apiKey);
        googlePlacesTextSearchResponseCall.enqueue(new Callback<GooglePlacesTextSearchResponse>() {
            @Override
            public void onResponse(Call<GooglePlacesTextSearchResponse> call,
                                   Response<GooglePlacesTextSearchResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Google place text search api call successful ->" + response.toString());
                    // First delete all records
                    deleteDataFromPointOfInterestResultTable();
                    // Now insert the new records
                    insertDataToPointOfInterestResultTable(response.body().getResults());
                } else {
                    Timber.e("Google place nearby api response not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesTextSearchResponse> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
            }
        });
    }

    private void insertDataToPointOfInterestResultTable(
            final List<TextSearchResult> textSearchResultList) { // Room does not allow operation on main thread
        Timber.d("insertDataToPointOfInterestResultTable is called");
        @SuppressLint("StaticFieldLeak")
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if(textSearchResultList != null) {
                    List<PointOfInterestResultEntity> pointOfInterestResultEntities = new ArrayList<>();
                    for(TextSearchResult textSearchResult : textSearchResultList) {
                        PointOfInterestResultEntity pointOfInterestResultEntity = new PointOfInterestResultEntity();
                        pointOfInterestResultEntity.id = textSearchResult.getId();
                        pointOfInterestResultEntity.pointOfInterestName = textSearchResult.getName();
                        if(textSearchResult.getGeometry() != null) {
                            pointOfInterestResultEntity.latitude = textSearchResult.getGeometry().getLocation().getLat();
                            pointOfInterestResultEntity.longitude = textSearchResult.getGeometry().getLocation().getLng();
                        } else {
                            pointOfInterestResultEntity.latitude = 0.0;
                            pointOfInterestResultEntity.longitude = 0.0;
                        }
                        pointOfInterestResultEntity.formattedAddress = textSearchResult.getFormattedAddress();
                        pointOfInterestResultEntity.placeId = textSearchResult.getPlaceId();
                        pointOfInterestResultEntity.rating = textSearchResult.getRating();
                        if(textSearchResult.getPhotos() != null) {
                            pointOfInterestResultEntity.photoReference = textSearchResult.getPhotos().get(0).getPhotoReference();
                        }
                        // Add the individual point of interest item to the list for bulk insert
                        pointOfInterestResultEntities.add(pointOfInterestResultEntity);
                    }
                    // Insert the point of interest list to the table
                    pointOfInterestResultDao.insertBulkPointOfInterestResultEntities(pointOfInterestResultEntities);
                } else {
                    Timber.e("Google Place text search api Json response is null");
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    private void deleteDataFromPointOfInterestResultTable() {
        Timber.d("deleteDataFromPointOfInterestResultTable is called");
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                pointOfInterestResultDao.deleteAllPointOfInterestResultEntities();
                return null;
            }
        };
        asyncTask.execute();
    }

    private void loadGooglePlaceCustomPlaceData(String placeId, String apiKey) {
        Timber.d("loadGooglePlaceCustomPlaceData is called");
        Call<GooglePlacesCustomPlaceDetailResponse> googlePlacesCustomPlaceDetailResponseCall =
                googlePlacesApiService.getGooglePlacesCustomPlaceDetails(placeId,apiKey);
        googlePlacesCustomPlaceDetailResponseCall.enqueue(new Callback<GooglePlacesCustomPlaceDetailResponse>() {
            @Override
            public void onResponse(Call<GooglePlacesCustomPlaceDetailResponse> call,
                                   Response<GooglePlacesCustomPlaceDetailResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Google place custom data call successful ->" + response.toString());
                    // Use postValue method as the Retrofit running on background thread
                    googlePlacesCustomPlaceDetail.postValue(response.body());
                } else {
                    Timber.e("Google place nearby api response not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesCustomPlaceDetailResponse> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
            }
        });
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
