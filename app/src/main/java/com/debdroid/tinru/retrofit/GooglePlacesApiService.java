package com.debdroid.tinru.retrofit;

import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.GooglePlacesNearbyResponse;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.GooglePlacesTextSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    Call<GooglePlacesNearbyResponse> getGooglePlacesNearbyResponse
            (@Query("location") String locationLatLng, @Query("radius") int radius,
             @Query("type") String type, @Query("key") String apiKey);

    @GET("maps/api/place/textsearch/json")
    Call<GooglePlacesTextSearchResponse> getGooglePlacesTextSearchResponse
            (@Query("query") String locationPointOfInterest, @Query("key") String apiKey);
}
