package com.debdroid.tinru.retrofit;

import com.debdroid.tinru.datamodel.AmadeusSandboxApi.AmadeusPointsOfInterestResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AmadeusSandboxPointOfInterestApiService {
    @GET("points-of-interest/yapq-search-text?image_size=medium&number_of_results=10")
    Call<AmadeusPointsOfInterestResponse> getAmadeusPointsOfInterestResponse
            (@Query("city_name") String city, @Query("apikey") String amadeusApiKey);
}

