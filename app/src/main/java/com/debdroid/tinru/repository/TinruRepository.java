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

    /**
     * This method returns the nearby search result to ViewModel as LiveData
     * @param latLng the latitude of the location
     * @param radius the radius to be searched
     * @param type the type of nearby attraction (i.e. restaurant, cafe, etc)
     * @param apiKey the api key
     * @param needFreshData whether data needs to be downloaded
     * @return the nearby searched result
     */
    public LiveData<List<NearbyResultEntity>> getNearbyResult(String latLng, int radius, String type,
                                                              String apiKey, boolean needFreshData) {
        Timber.d("getNearbyResult is called");
        // Load the data only when ViewModel needs it
        if (needFreshData) {
            loadGooglePlaceNearbyData(latLng, radius, type, apiKey);
        }
        // Return current data from the database
        return nearbyResultDao.loadAllNearbyResultEntitiesAsLiveData();
    }

    /**
     * This method returns the points of interest search result to ViewModel as LiveData
     * @param locationPointOfInterest the location along with 'point of interest' string
     * @param apiKey the api key
     * @param needFreshData whether data needs to be downloaded
     * @return the points of interest searched result
     */
    public LiveData<List<PointOfInterestResultEntity>> getPointOfInterestResult(
            String locationPointOfInterest, String apiKey, boolean needFreshData) {
        Timber.d("getPointOfInterestResult is called");
        // Load the data only when ViewModel needs it
        if (needFreshData) {
            // First delete all records
            deleteDataFromPointOfInterestResultTable();
            loadGooglePlacePointOfInterestData(locationPointOfInterest, apiKey);
        }
        // Return current data from the database
        return pointOfInterestResultDao.loadAllPointOfInterestResultEntitiesAsLiveData();
    }

    /**
     * This method returns the place id search result to ViewModel as LiveData
     * @param placeId the place id to be searched
     * @param apiKey the api key
     * @param needFreshData whether data needs to be downloaded
     * @return the searched result of the place id
     */
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

    /**
     * This method returns the airport code result to ViewModel as LiveData
     * @param latitude the latitude of the location
     * @param longitude the longitude of hte location
     * @param apiKey the api key
     * @param needFreshData whether data needs to be downloaded
     * @return the searched airport data
     */
    public LiveData<String> getAirportCode(double latitude, double longitude, String apiKey, boolean needFreshData) {
        Timber.d("getAirportCode is called");
        // Need fresh data, so make the mutable variable null
        if (needFreshData) airportCode = null;

        // Load the data only when ViewModel needs it
        if (airportCode == null) {
            airportCode = new MutableLiveData<>();
            loadAmadeusSandboxAirportData(latitude, longitude, apiKey);
        }
        return airportCode;
    }

    /**
     * This method returns the low fare flight search result to ViewModel as LiveData
     * @param origin the origin of the flight
     * @param destination the destination of hte flight
     * @param nonStop nonstop flight flag
     * @param departureDate the departure date
     * @param apiKey api key
     * @param needFreshData whether data needs to be downloaded
     * @return the searched low fare flight data
     */
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

    /**
     * This method calls Google Places API using retrofit for nearby attractions
     * @param latLng latitude and longitude of the location
     * @param radius radius of the are to be searched
     * @param type nearby search type (i.e. restaurant, cafe, etc)
     * @param apiKey api key of Google places api
     */
    private void loadGooglePlaceNearbyData(String latLng, int radius, String type, String apiKey) {
        Timber.d("loadGooglePlaceNearbyData is called");
        Call<GooglePlacesNearbyResponse> googlePlacesNearbyResponseCall =
                googlePlacesApiService.getGooglePlacesNearbyResponse(latLng, radius, type, apiKey);
        googlePlacesNearbyResponseCall.enqueue(new Callback<GooglePlacesNearbyResponse>() {
            @Override
            public void onResponse(Call<GooglePlacesNearbyResponse> call, Response<GooglePlacesNearbyResponse> response) {
                Timber.d("Processing in thread -> " + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Timber.d("Google places nearby api call successful ->" + response.toString());
                    // First delete all records
                    deleteDataFromNearbyResultTable();
                    // Now insert the new records
                    insertDataToNearbyResultTable(response.body().getResults());
                } else {
                    Timber.e("Google places nearby api response not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesNearbyResponse> call, Throwable t) {
                Timber.e("Google places nearby api call. Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });
    }


    /**
     * This method calls Google Places API using retrofit for points of interest for a location
     * @param locationPointOfInterest location name with 'point of interest' string (used for api search)
     * @param apiKey api key of Google places api
     */
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
                    Timber.d("Google places text search api call successful ->" + response.toString());
                    // Now insert the new records
                    insertDataToPointOfInterestResultTable(response.body().getResults());
                } else {
                    Timber.e("Google places nearby api response not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesTextSearchResponse> call, Throwable t) {
                Timber.e("Google places nearby api call. Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });
    }

    /**
     * This method calls Google Places API using retrofit for place details of a place id
     * @param placeId the id of the place
     * @param apiKey api key of Google places api
     */
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
                    Timber.d("Google places custom data call successful ->" + response.toString());
                    // Use postValue method as the Retrofit running on background thread
                    googlePlacesCustomPlaceDetail.postValue(response.body());
                } else {
                    Timber.e("Google places nearby api response not successful -> " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<GooglePlacesCustomPlaceDetailResponse> call, Throwable t) {
                Timber.e("Google places nearby api. Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });
    }

    /**
     * This method calls AmadeusSandbox api using retrofit to find out an airport
     * @param latitude the latitude of the area
     * @param longitude the longitude of the area
     * @param apiKey the api key of AmadeusSandbox api service
     */
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
                Timber.e("Amadeus sandbox airport data call. Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });

    }

    /**
     * This method calls AmadeusSandbox api using retrofit to find out low fare flights
     * @param origin the origin of the flight
     * @param destination the destination of the flight
     * @param nonStop flag to indicate nonstop flight
     * @param departureDate departure date of the flight
     * @param apiKey the api key of AmadeusSandbox api service
     */
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
                Timber.e("Amadeus sandbox low fare data call. Error happened -> " + t.toString());
                t.printStackTrace();
            }
        });

    }

    /**
     * This method inserts nearby searched data to database
     * @param resultList the nearby searched result
     */
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

    /**
     * This method deletes the existing nearby search records from the database
     */
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

    /**
     * This method inserts points of interest data to database
     * @param textSearchResultList the point of interest searched result
     */
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
                    Timber.e("Google Places text search api Json response is null");
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    /**
     * This method deletes the points of interest data from the database
     */
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

    /**
     * This method inserts the user searched location details to the database which is for widget
     * @param location the searched location
     * @param airportCode the airport code of the locations
     * @param lat the latitude of the location
     * @param lng the longitude of the location
     * @param datetimestamp the system generated date and time stamp
     */
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
}
