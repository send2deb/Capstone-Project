package com.debdroid.tinru.retrofit;

import com.debdroid.tinru.datamodel.AmadeusSandboxAirportSearchApi.AmadeusSandboxAirportSearchResponse;
import com.debdroid.tinru.datamodel.AmadeusSandboxApi.AmadeusPointsOfInterestResponse;
import com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi.AmadeusSandboxLowFareSearchResponse;
import com.debdroid.tinru.utility.NetworkUtility;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AmadeusSandboxApiService {
    //Use full url to override the base url of Retrofit (FYI - base url is for Google Places api)

    @GET("https://api.sandbox.amadeus.com/v1.2/points-of-interest/yapq-search-text?image_size=medium&number_of_results=10")
    Call<AmadeusPointsOfInterestResponse> getAmadeusPointsOfInterestResponse
            (@Query("city_name") String city, @Query("apikey") String apiKey);

    @GET("https://api.sandbox.amadeus.com/v1.2/airports/nearest-relevant")
    Call<List<AmadeusSandboxAirportSearchResponse>> getAmadeusAirportSearchResponse
            (@Query("latitude") double latitude, @Query("longitude") double longitude,
             @Query("apikey") String apiKey);

    @GET("https://api.sandbox.amadeus.com/v1.2/flights/low-fare-search")
    Call<AmadeusSandboxLowFareSearchResponse> getAmadeusLowFareSearchResponse
            (@Query("origin") String origin, @Query("destination") String destination,
             @Query("nonstop") boolean nonstopFlag, @Query("departure_date") String departure_date,
             @Query("apikey") String amadeusApiKey);
}

