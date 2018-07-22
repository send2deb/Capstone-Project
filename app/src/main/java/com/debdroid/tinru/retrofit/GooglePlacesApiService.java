package com.debdroid.tinru.retrofit;

import com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi.GooglePlacesCustomPlaceDetailResponse;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.GooglePlacesNearbyResponse;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.GooglePlacesTextSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlacesApiService {
    @GET("nearbysearch/json")
    Call<GooglePlacesNearbyResponse> getGooglePlacesNearbyResponse
            (@Query("location") String locationLatLng, @Query("radius") int radius,
             @Query("type") String type, @Query("key") String apiKey);

    @GET("textsearch/json")
    Call<GooglePlacesTextSearchResponse> getGooglePlacesTextSearchResponse
            (@Query("query") String locationPointOfInterest, @Query("key") String apiKey);

    @GET("details/json?fields=url,website,reviews,opening_hours")
    Call<GooglePlacesCustomPlaceDetailResponse> getGooglePlacesCustomPlaceDetails
            (@Query("placeid") String placeId, @Query("key") String apiKey);
}
