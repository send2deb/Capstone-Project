package com.debdroid.tinru.retrofit;

import com.debdroid.tinru.datamodel.GooglePlacesNearbyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlacesNearbySearchApiService {
    @GET("maps/api/place/nearbysearch/json")
    Call<GooglePlacesNearbyResponse> getGooglePlacesNearbyResponse
            (@Query("location") String locationLatLng, @Query("radius") int radius,
             @Query("type") String type, @Query("key") String apiKey);
}
