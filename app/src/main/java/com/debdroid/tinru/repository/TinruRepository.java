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
import com.debdroid.tinru.database.UserSearchedLocationDao;
import com.debdroid.tinru.database.UserSearchedLocationEntity;
import com.debdroid.tinru.datamodel.AmadeusSandboxAirportSearchApi.AmadeusSandboxAirportSearchResponse;
import com.debdroid.tinru.datamodel.AmadeusSandboxApi.AmadeusPointsOfInterestResponse;
import com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi.AmadeusSandboxLowFareSearchResponse;
import com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi.GooglePlacesCustomPlaceDetailResponse;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.GooglePlacesNearbyResponse;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.Result;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.GooglePlacesTextSearchResponse;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.TextSearchResult;
import com.debdroid.tinru.retrofit.AmadeusSandboxApiService;
import com.debdroid.tinru.retrofit.GooglePlacesApiService;
import com.debdroid.tinru.utility.RepositoryUtility;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@TinruCustomScope.TinruApplicationScope
//Let Dagger know that this class should be constructed only once
public class TinruRepository {
    private AmadeusSandboxApiService amadeusSandboxApiService;
    private GooglePlacesApiService googlePlacesApiService;
    private NearbyResultDao nearbyResultDao;
    private PointOfInterestResultDao pointOfInterestResultDao;
    private UserSearchedLocationDao userSearchedLocationDao;
    private MutableLiveData<GooglePlacesCustomPlaceDetailResponse> googlePlacesCustomPlaceDetail;
    private MutableLiveData<String> airportCode;
    private MutableLiveData<AmadeusSandboxLowFareSearchResponse> amadeusSandboxLowFareSearchResult;

    @Inject
    public TinruRepository(AmadeusSandboxApiService amadeusSandboxApiService,
                           GooglePlacesApiService googlePlacesApiService, NearbyResultDao nearbyResultDao,
                           PointOfInterestResultDao pointOfInterestResultDao,
                           UserSearchedLocationDao userSearchedLocationDao) {
        this.amadeusSandboxApiService = amadeusSandboxApiService;
        this.googlePlacesApiService = googlePlacesApiService;
        this.nearbyResultDao = nearbyResultDao;
        this.pointOfInterestResultDao = pointOfInterestResultDao;
        this.userSearchedLocationDao = userSearchedLocationDao;
    }

    public LiveData<List<NearbyResultEntity>> getNearbyResult(String latLng, int radius, String type,
                                                              String apiKey, boolean needFreshData) {
        Timber.d("getNearbyResult is called");
        // Load the data only when ViewModel need it
        if (needFreshData) {
            loadGooglePlaceNearbyData(latLng, radius, type, apiKey);
        }
        // Return current data from the database
        return nearbyResultDao.loadAllNearbyResultEntitiesAsLiveData();
    }

    public LiveData<List<PointOfInterestResultEntity>> getPointOfInterestResult(
            String locationPointOfInterest, String apiKey, boolean needFreshData) {
        Timber.d("getPointOfInterestResult is called");
        // Load the data only when ViewModel need it
        if (needFreshData) {
            // First delete all records
            deleteDataFromPointOfInterestResultTable();
            loadGooglePlacePointOfInterestData(locationPointOfInterest, apiKey);
        }
        // Return current data from the database
        return pointOfInterestResultDao.loadAllPointOfInterestResultEntitiesAsLiveData();
    }

    public LiveData<GooglePlacesCustomPlaceDetailResponse> getGooglePlaceCustomPlaceData(
            String placeId, String apiKey, boolean needFreshData) {
        Timber.d("getGooglePlaceCustomPlaceData is called");
        // Need fresh data, so make the mutable variable null
        if (needFreshData) googlePlacesCustomPlaceDetail = null;

        // Load the data only when ViewModel need it
        if (googlePlacesCustomPlaceDetail == null) {
            googlePlacesCustomPlaceDetail = new MutableLiveData<>();
            loadGooglePlaceCustomPlaceData(placeId, apiKey);
        }
        return googlePlacesCustomPlaceDetail;
    }

    public LiveData<String> getAirportCode(double latitude, double longitude, String apiKey, boolean needFreshData) {
        Timber.d("getAirportCode is called");
        // Need fresh data, so make the mutable variable null
        if (needFreshData) airportCode = null;

        // Load the data only when ViewModel need it
        if (airportCode == null) {
            airportCode = new MutableLiveData<>();
            loadAmadeusSandboxAirportData(latitude, longitude, apiKey);
        }
        return airportCode;
    }

    public LiveData<AmadeusSandboxLowFareSearchResponse> getLowFareFlights(String origin, String destination,
                  boolean nonStop, String departureDate, String apiKey, boolean needFreshData) {
        Timber.d("getLowFareFlights is called");
        // Need fresh data, so make the mutable variable null
        if (needFreshData) amadeusSandboxLowFareSearchResult = null;

        // Load the data only when ViewModel need it
        if (amadeusSandboxLowFareSearchResult == null) {
            amadeusSandboxLowFareSearchResult = new MutableLiveData<>();
            loadAmadeusSandboxLowFareSearchData(origin, destination, nonStop, departureDate, apiKey);
        }
        return amadeusSandboxLowFareSearchResult;
    }

    public void addSearchedLocationData(String location, String airportCode,
                                        double lat, double lng, Date datetimestamp) {
        insertDataToUserSearchedLocationTable(location, airportCode, lat, lng, datetimestamp);
    }

    private void loadGooglePlaceNearbyData(String latLng, int radius, String type, String apiKey) {
        Timber.d("loadGooglePlaceNearbyData is called");
        Call<GooglePlacesNearbyResponse> googlePlacesNearbyResponseCall =
                googlePlacesApiService.getGooglePlacesNearbyResponse(latLng, radius, type, apiKey);
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
                t.printStackTrace();
            }
        });
    }


    private void loadGooglePlacePointOfInterestData(String locationPointOfInterest, String apiKey) {
        Timber.d("loadGooglePlacePointOfInterestData is called");
        Call<GooglePlacesTextSearchResponse> googlePlacesTextSearchResponseCall =
                googlePlacesApiService.getGooglePlacesTextSearchResponse(locationPointOfInterest, apiKey);
        googlePlacesTextSearchResponseCall.enqueue(new Callback<GooglePlacesTextSearchResponse>() {
            @Override
            public void onResponse(Call<GooglePlacesTextSearchResponse> call,
                                   Response<GooglePlacesTextSearchResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Google place text search api call successful ->" + response.toString());
//                    // First delete all records
//                    deleteDataFromPointOfInterestResultTable();
                    // Now insert the new records
                    insertDataToPointOfInterestResultTable(response.body().getResults());
                } else {
                    Timber.e("Google place nearby api response not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesTextSearchResponse> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });
    }

    private void loadGooglePlaceCustomPlaceData(String placeId, String apiKey) {
        Timber.d("loadGooglePlaceCustomPlaceData is called");
        Call<GooglePlacesCustomPlaceDetailResponse> googlePlacesCustomPlaceDetailResponseCall =
                googlePlacesApiService.getGooglePlacesCustomPlaceDetails(placeId, apiKey);
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
                t.printStackTrace();
            }
        });
    }

    private void loadAmadeusSandboxAirportData(double latitude, double longitude, String apiKey) {
        Timber.d("loadAmadeusSandboxAirportData is called");
        Call<List<AmadeusSandboxAirportSearchResponse>> amadeusSandboxAirportSearchResponseCall =
                amadeusSandboxApiService.getAmadeusAirportSearchResponse(latitude, longitude, apiKey);
        amadeusSandboxAirportSearchResponseCall.enqueue(new Callback<List<AmadeusSandboxAirportSearchResponse>>() {
            @Override
            public void onResponse(Call<List<AmadeusSandboxAirportSearchResponse>> call, Response<List<AmadeusSandboxAirportSearchResponse>> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Amadeus sandbox airport data call successful ->" + response.toString());
                    // Use postValue method as the Retrofit running on background thread
                    airportCode.postValue(response.body().get(0).getCity());
                    Timber.d("Airport code -> " + response.body().get(0).getCity());
                } else {
                    Timber.e("Amadeus sandbox airport data call not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<List<AmadeusSandboxAirportSearchResponse>> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });

    }

    private void loadAmadeusSandboxLowFareSearchData(String origin, String destination, boolean nonStop,
                                                     String departureDate, String apiKey) {
        Timber.d("loadAmadeusSandboxLowFareSearchData is called");
        Call<AmadeusSandboxLowFareSearchResponse> sandboxLowFareSearchResponseCall =
                amadeusSandboxApiService.getAmadeusLowFareSearchResponse(origin, destination, nonStop,
                        departureDate, apiKey);
        sandboxLowFareSearchResponseCall.enqueue(new Callback<AmadeusSandboxLowFareSearchResponse>() {
            @Override
            public void onResponse(Call<AmadeusSandboxLowFareSearchResponse> call, Response<AmadeusSandboxLowFareSearchResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Amadeus sandbox low fare data call successful ->" + response.toString());
                    // Use postValue method as the Retrofit running on background thread
                    amadeusSandboxLowFareSearchResult.postValue(response.body());
                    Timber.d("currency -> " + response.body().getCurrency());
                } else {
                    Timber.e("Amadeus sandbox low fare data call not successful -> " + response.raw().message());
                    // Use postValue method as the Retrofit running on background thread
                    // Set it to null to indicate error
                    amadeusSandboxLowFareSearchResult.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<AmadeusSandboxLowFareSearchResponse> call, Throwable t) {
                Timber.e("Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });

    }

    private void insertDataToNearbyResultTable(final List<Result> resultList) { // Room does not allow operation on main thread
        Timber.d("insertDataToNearbyResultTable is called");
        @SuppressLint("StaticFieldLeak") final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (resultList != null) {
                    List<NearbyResultEntity> nearbyResultEntities =
                            RepositoryUtility.buildNearbyResultEntity(resultList);
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
        @SuppressLint("StaticFieldLeak") final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                nearbyResultDao.deleteAllNearbyResultEntities();
                return null;
            }
        };
        asyncTask.execute();
    }

    private void insertDataToPointOfInterestResultTable(
            final List<TextSearchResult> textSearchResultList) { // Room does not allow operation on main thread
        Timber.d("insertDataToPointOfInterestResultTable is called");
        @SuppressLint("StaticFieldLeak") final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (textSearchResultList != null) {
                    List<PointOfInterestResultEntity> pointOfInterestResultEntities =
                            RepositoryUtility.buildPointOfInterestResultEntity(textSearchResultList);
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
        @SuppressLint("StaticFieldLeak") final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                pointOfInterestResultDao.deleteAllPointOfInterestResultEntities();
                return null;
            }
        };
        asyncTask.execute();
    }

    private void insertDataToUserSearchedLocationTable(String location, String airportCode, double lat,
                                                       double lng, Date datetimestamp) {
        Timber.d("insertDataToUserSearchedLocationTable is called");
        final UserSearchedLocationEntity userSearchedLocationEntity =
                RepositoryUtility.buildUserSearchedLocationEntity(location, airportCode, lat, lng, datetimestamp);

        // Room does not allow operation on main thread
        @SuppressLint("StaticFieldLeak") final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userSearchedLocationDao.insertSingleLocation(userSearchedLocationEntity);
                return null;
            }
        };
        asyncTask.execute();
    }

    public void getPointsOfInterest() {
        String city = "London";
        String key = "";
        Call<AmadeusPointsOfInterestResponse> amadeusPointsOfInterestResponseCall =
                amadeusSandboxApiService.getAmadeusPointsOfInterestResponse(city, key);
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
                t.printStackTrace();
            }
        });
    }
}
