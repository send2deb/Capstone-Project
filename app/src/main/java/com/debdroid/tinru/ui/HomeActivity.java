package com.debdroid.tinru.ui;

import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.debdroid.tinru.R;
import com.debdroid.tinru.repository.TinruRepository;
import com.debdroid.tinru.utility.CommonUtility;
import com.debdroid.tinru.utility.NetworkUtility;
import com.debdroid.tinru.viewmodel.HomeViewModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    @Inject
    TinruRepository tinruRepository;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.tv_current_place_city_country)
    TextView currentCityCountryTextView;
    @BindView(R.id.bt_location_search)
    Button locationSearchButton;
    @BindView(R.id.pb_home_activity)
    ProgressBar progressBar;
    @BindView(R.id.tv_progressbar_text_msg)
    TextView progressBarTextMsg;
    @BindView(R.id.home_activity_linear_layout)
    LinearLayout linearLayout;
    @BindView(R.id.iv_current_place_image)
    ImageView currentPlaceImage;
    @BindView(R.id.fab_home)
    FloatingActionButton fab;
    @BindView(R.id.home_toolbar)
    Toolbar toolbar;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private boolean mLocationPermissionGranted;
    private int PLACE_PICKER_REQUEST = 1;
    private String currentPlaceName;
    private String currentPlaceId;
    private LatLng currentPlaceLatLng;
    private String currentPlaceCity;
    private String currentPlaceCountry;
    private String currentPostalCode;
    private String currentPlacePhotoAtbrCityCountry = null;
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // If the device is not online then show a message and return
        // Use progress bar message to show no internet connection
        if(!NetworkUtility.isOnline(this)) {
            progressBarTextMsg.setVisibility(TextView.VISIBLE);
            progressBarTextMsg.setText(getString(R.string.no_network_error_msg));
            progressBar.setVisibility(ProgressBar.INVISIBLE); // Hide the progressbar
            linearLayout.setVisibility(LinearLayout.INVISIBLE); // Hide the linear layout
            fab.setVisibility(FloatingActionButton.INVISIBLE); // Hide the fab
            return;
        } else { // Make sure the message is replaced properly when device is online
            progressBarTextMsg.setText(getString(R.string.home_progressbar_text_msg));
        }

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Show the progress bar and hide the layout & fab
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressBarTextMsg.setVisibility(TextView.VISIBLE);
        linearLayout.setVisibility(LinearLayout.INVISIBLE);
        fab.setVisibility(FloatingActionButton.INVISIBLE);

        // Create the viewmodel
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeViewModel.class);

        // Get current location of the device
        getDeviceLocation();

        // Start transition
//        startTransition();
    }

    /**
     * Handle search button action
     *
     * @param view The associated view
     */
    @OnClick(R.id.bt_location_search)
    public void locationSearchAction(View view) {
        Timber.d("locationSearchAction is called");
        openPlacePicker();
    }

    /**
     * Handle click action of NearbyRestaurant button
     *
     * @param view Associated view
     */
    @OnClick(R.id.bt_nearby_restaurant)
    public void nearbyRestaurantAction(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_restaurant),
                getString(R.string.home_activity_layout_nearby_restaurant));
    }

    /**
     * Handle click action of NearbyCafe button
     *
     * @param view Associated view
     */
    @OnClick(R.id.bt_nearby_cafe)
    public void nearbyCafeAction(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_cafe),
                getString(R.string.home_activity_layout_nearby_cafe));
    }

    /**
     * Handle click action of NearbyBar button
     *
     * @param view Associated view
     */
    @OnClick(R.id.bt_nearby_bar)
    public void nearbyBarAction(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_bar),
                getString(R.string.home_activity_layout_nearby_bar));
    }

    /**
     * Handle click action of NearbyATM button
     *
     * @param view Associated view
     */
    @OnClick(R.id.bt_nearby_atm)
    public void nearbyATMAction(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_atm),
                getString(R.string.home_activity_layout_nearby_atm));
    }

    /**
     * Handle click action of NearbyShopping button
     *
     * @param view Associated view
     */
    @OnClick(R.id.bt_nearby_shopping)
    public void nearbyShoppingAction(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_shopping),
                getString(R.string.home_activity_layout_nearby_shopping));
    }

    /**
     * Handle click action of NearbyPetrolPump button
     *
     * @param view Associated view
     */
    @OnClick(R.id.bt_nearby_petrol_pump)
    public void nearbyPetrolPumpAction(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_petrol_pump),
                getString(R.string.home_activity_layout_nearby_petrol_pump));
    }

    /**
     * Handle click action of fab button
     *
     * @param view Associated view
     */
    @OnClick(R.id.fab_home)
    public void fabAction(View view) {
        openPlacePicker();
    }


    /**
     * Open Google Places Api Place Picker to select a place
     */
    private void openPlacePicker() {
        Timber.d("openPlacePicker is called");
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Timber.e("Google Play Services error: " + e.getMessage());
            Timber.e(e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Timber.e("Google Play Services not available: " + e.getMessage());
            Timber.e(e);
        }
    }

    /**
     * This method is called when Google place picker returns the result
     *
     * @param requestCode The request code used for the place picker request
     * @param resultCode  CustomPlaceDetailResult code of Google Place Picker intent
     * @param data        Google Place picker Intent data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult is called");
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Timber.d(String.format("Place: %s", place.getName()));
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                List<Integer> placeTypes = place.getPlaceTypes();
                for (int type : placeTypes) {
                    Timber.d("Place Type -> " + type);
                }
                LatLng placeLatLng = place.getLatLng();
                Timber.d("Lat -> " + placeLatLng.latitude);
                Timber.d("Long -> " + placeLatLng.longitude);
                // Load airport code of destination
                viewModel.getAirportCode(placeLatLng.latitude, placeLatLng.longitude,
                        getString(R.string.amadeus_sandbox_key)).observe(this, airportCode -> {
                    Timber.d("Destination airport code -> " + airportCode);
                    CommonUtility.saveDataInSharedPreference(sharedPreferences,
                            getString(R.string.preference_destination_airport_code_key), airportCode);
                    // Start place of interest activity - start activity only after the airport code is retrieved
                    prepareAndStartPointOfInterestListActivity(place.getName().toString(), airportCode,
                            placeLatLng.latitude, placeLatLng.longitude);
                });
            }
        }
    }

    /**
     * Gets the current location of the device using Google Place Api SDK
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        Timber.d("getDeviceLocation is called");

        //First ask for permission
        getLocationPermission();

        try {
            if (mLocationPermissionGranted) {
                Task<PlaceLikelihoodBufferResponse> placeResult =
                        mPlaceDetectionClient.getCurrentPlace(null);
                placeResult.addOnCompleteListener(task -> {
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Timber.d(String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }

                    // Pick the place which has maximum likelihood value
                    Place place = likelyPlaces.get(0).getPlace();
                    float placeLikelihoodValue = likelyPlaces.get(0).getLikelihood();
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        if (placeLikelihood.getLikelihood() > placeLikelihoodValue) {
                            place = placeLikelihood.getPlace();
                            placeLikelihoodValue = placeLikelihood.getLikelihood();
                        }
                    }
                    currentPlaceName = place.getName().toString();
                    currentPlaceId = place.getId();
                    currentPlaceLatLng = place.getLatLng();

                    // Get the address of the location for city and country name
                    Address address = getAddressFromGeoCoder(currentPlaceLatLng.latitude,
                            currentPlaceLatLng.longitude);
                    if(address != null) {
                        currentPlaceCity = address.getLocality();
                        currentPlaceCountry = address.getCountryName();
                        currentPostalCode = address.getPostalCode();
                        Timber.d(String.format("Place '%s' of '%s', '%s' , '%s' has likelihood: %g",
                                currentPlaceName, currentPlaceCity, currentPlaceCountry,
                                currentPostalCode, placeLikelihoodValue));
                    } else { // Use the place name instead if the address is null
                        currentPlaceCity = currentPlaceName;
                    }

                    startTransition();

                    // Update the ui with the details
                    updateUi();

                    // Save city to SharedPreference - used for Flight list activity
                    CommonUtility.saveDataInSharedPreference(sharedPreferences,
                            getString(R.string.preference_origin_airport_city_key), currentPlaceCity);

                    // Load airport code of current place - used for Flight list activity
                    viewModel.getAirportCode(currentPlaceLatLng.latitude, currentPlaceLatLng.longitude,
                            getString(R.string.amadeus_sandbox_key)).observe(this, airportCode -> {
                        Timber.d("Current place airport code -> " + airportCode);
                        CommonUtility.saveDataInSharedPreference(sharedPreferences,
                                getString(R.string.preference_origin_airport_code_key), airportCode);
                    });

                    // Release the buffer to avoid memory leak
                    likelyPlaces.release();
                });
            }
        } catch (SecurityException e) {
            Timber.e("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find out the locality, country and other details of the picked up place using Geocoder
     * @param latitude The latitude of the place
     * @param longitude The longitude of the place
     * @return Address of the place
     */
    private Address getAddressFromGeoCoder(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,
                    longitude, 1);
            if (addresses.size() > 0) {
                address = addresses.get(0);
            } else {
                Timber.d("No address found for place  -> " + currentPlaceName);
            }
        } catch (IOException e) {
            Timber.e("Geocoder error: " + e.getMessage());
            e.printStackTrace();
        }
        return address;
    }

    /**
     * Update ui with the data
     */
    private void updateUi() {
        Timber.d("updateUi is called");
        // Load the photo of local place
        loadLocalPlacePhoto();
        // Set the current City & Country
        if (currentPlaceCity != null && currentPlaceCountry != null) {
            // As per Google Place Api guideline - show the currentPlaceName which is attribution of the photo
            currentPlacePhotoAtbrCityCountry = String.format("%s, %s, %s",currentPlaceName,
                    currentPlaceCity, currentPlaceCountry);
        } else if (currentPlaceCountry == null) {
            currentPlacePhotoAtbrCityCountry = currentPlaceCity;
        } // No 'else' for 'currentPlaceCity == null' because it will have currentPlaceName if it's null
          // based on the logic in getDeviceLocation()
        currentCityCountryTextView.setText(currentPlacePhotoAtbrCityCountry);

        // Hide the progress bar and show the layout & fab
        progressBar.setVisibility(ProgressBar.GONE);
        progressBarTextMsg.setVisibility(TextView.GONE);
        linearLayout.setVisibility(LinearLayout.VISIBLE);
        fab.setVisibility(FloatingActionButton.VISIBLE);
    }

    /**
     * Load the photo of the current place using Google Places Api SDK
     */
    private void loadLocalPlacePhoto() {
        Timber.d(("loadLocalPlacePhoto is called. currentPlaceId -> " + currentPlaceId));
        // TODO need to check if bitmap loading needs to be optimised further
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(currentPlaceId);
        photoMetadataResponse.addOnCompleteListener(task -> {
            // Get the list of photos.
            PlacePhotoMetadataResponse photos = task.getResult();
            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
            // Get the first photo in the list.
            if (photoMetadataBuffer.getCount() > 0) {
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                //TODO need to add the attribution for all Google Place Api usage
                CharSequence attribution = photoMetadata.getAttributions();
                Timber.e("The photo attribution -> " + attribution.toString());
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(task1 -> {
                    PlacePhotoResponse photo = task1.getResult();
                    Bitmap bitmap = photo.getBitmap();
                    currentPlaceImage.setImageBitmap(bitmap);
                });
            } else {
                Timber.d("No photo found for place id -> " + currentPlaceId);
            }
            // Release the buffer to avoid memory leak
            photoMetadataBuffer.release();
        });
    }

//    /**
//     * Save data to SharedPreference
//     * @param key The key of the SharedPreference data
//     * @param value The value of the SharedPReference data
//     */
//    private void saveDataInSharedPreference(String key, String value) {
//        Timber.d("saveDataInSharedPreference is called");
//        Timber.d("saveDataInSharedPreference:key - " + key);
//        Timber.d("saveDataInSharedPreference:value - " + value);
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(key, value);
//        editor.apply(); // Write asynchronously
//    }

    private void startTransition() {
        Timber.d("startTransition is called");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.BOTTOM);
            slide.addTarget(R.id.nearby_button_container_linear_layout);
            slide.setInterpolator(AnimationUtils.loadInterpolator(
                    this,
                    android.R.interpolator.linear_out_slow_in));
            slide.setDuration(5000);
            getWindow().setEnterTransition(slide);
        }
    }

    /**
     * Start Nearby activity
     *
     * @param type     The type for which the Nearby activity is to be started
     * @param typeName The user friendly nme of the corresponding type
     */
    private void startNearbyGridActivity(String type, String typeName) {
        Intent intent = new Intent(this, NearByGridActivity.class);
        String latLng = String.format("%s,%s", currentPlaceLatLng.latitude, currentPlaceLatLng.longitude);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_LOCATION, currentPlaceCity);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_LATLNG, latLng);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_RADIUS,
                getResources().getInteger(R.integer.nearby_search_radius_meter));
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE, type);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE_NAME, typeName);
        startActivity(intent);
    }

    /**
     * Start point of interest list activity. Store data before starting the activity
     *
     * @param latitude  The latitude of the point of interest
     * @param longitude The longitude of the point of interest
     */
    private void prepareAndStartPointOfInterestListActivity(String placeName, String airportCode,
                                                            double latitude, double longitude) {
        String poiLocation = null;
        Address address = getAddressFromGeoCoder(latitude, longitude);
        if(address != null) poiLocation = address.getLocality();

        // If city is null then use the place name returned by the Place picker
        if(poiLocation == null) {
            Timber.w("Location is null! Using place name instead -> " + placeName);
            poiLocation = placeName;
        }

        // Save city to SharedPreference - used for Flight list activity
        CommonUtility.saveDataInSharedPreference(sharedPreferences,
                getString(R.string.preference_destination_airport_city_key), poiLocation);

        // Save data to UserSearchedLocation table - used for Widget
        Date date = new Date();
        viewModel.addSearchedLocationData(poiLocation, airportCode, latitude, longitude, date);

        // Now start the activity
        Intent intent = new Intent(this, PointOfInterestListActivity.class);
        intent.putExtra(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_LOCATION, poiLocation);
        intent.putExtra(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_AIRPORT_CODE, airportCode);
        intent.putExtra(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_IS_APPWIDGET_INTENT, false);
        startActivity(intent);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        Timber.d("getLocationPermission is called");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Timber.d("onRequestPermissionsResult is called");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        // Call getDeviceLocation() - this will be for the first time only when the permission is granted
        getDeviceLocation();
    }
}
