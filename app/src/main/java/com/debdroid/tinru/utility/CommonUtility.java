package com.debdroid.tinru.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.debdroid.tinru.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class CommonUtility {
    /**
     * This utility method returns the current date
     * @return Today's date in yyyy-MM-dd format
     */
    public static String getTodayDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = simpleDateFormat.format(new Date());
        return todayDate;
    }

    /**
     * This utility method returns the date which is 2 days ahead of today's date  in simple yyyy-MM-dd format
     * @return Date which is 3 days ahead of Today's date in yyyy-MM-dd format
     */
    public static String getSimpleTwoDayFutureDate() {
        // Set the calendar to current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 2);
        Date date = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String twoDayFutureDate = simpleDateFormat.format(date);
        return twoDayFutureDate;
    }

    /**
     * This utility method returns the date parameter format for Amadeus Sandbox low fare search
     * @return Formatted date parameter for Amadeus Sandbox api call
     */
    public static String getAmadeusLowFareFlightSearchDateFormat() {
        String startDate = getTodayDate();
        String endDate = getSimpleTwoDayFutureDate();
        String dateParameter = startDate.concat("--").concat(endDate);
        return dateParameter;
    }

    /**
     * This utility method builds the url for Google Places Api photo call
     * @param context Activity context
     * @param maxWidth Width of the photo
     * @param photoReference Photo reference
     * @return Google Place photo api
     */
    public static String buildGooglePlacesPhotoUrl(Context context, String maxWidth, String photoReference) {
        String photoReferenceUrlString = null;
        Uri uri = Uri.parse(NetworkUtility.getGooglePlaceApiBaseUrl())
                .buildUpon()
                .appendPath(context.getString(R.string.google_places_api_path_segment_photo))
                .appendQueryParameter(context.getString(R.string.google_places_api_photo_query_parm_maxwidth_key),
                        maxWidth)
                .appendQueryParameter(context.getString(R.string.google_places_api_query_apikey),
                        context.getString(R.string.google_maps_key))
                .appendQueryParameter(context.getString(R.string.google_places_api_photo_query_parm_photoreference_key),
                        photoReference)
                .build();
        try {
            URL url = new URL(uri.toString());
            photoReferenceUrlString = url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return photoReferenceUrlString;
    }

    /**
     * Save data to SharedPreference
     * @param key The key of the SharedPreference data
     * @param value The value of the SharedPReference data
     */
    public static void saveDataInSharedPreference(SharedPreferences sharedPreferences ,String key, String value) {
        Timber.d("saveDataInSharedPreference is called");
        Timber.d("saveDataInSharedPreference:key - " + key);
        Timber.d("saveDataInSharedPreference:value - " + value);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply(); // Write asynchronously
    }
}
