package com.debdroid.tinru.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.debdroid.tinru.R;
import com.debdroid.tinru.repository.TinruRepository;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

//    @Inject
//    TinruRepository tinruRepository;

    @BindView(R.id.bt_location_search)
    Button locationSearchButton;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // A default location (London, UK) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(51.5074, 0.1278);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    // Google place picker request number
    private int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        //tinruRepository.getPointsOfInterest();

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //getDeviceLocation();

//        tinruRepository.getGooglePlaceNearbyData("51.5073509,-0.1277583",
//                1000,
//                "cafe",
//                getString(R.string.google_maps_key));
    }

    /**
     * Open Google Places Api Place Picker to select a place
     * @param view
     */
    @OnClick(R.id.bt_location_search)
    public void openPlacePicker(View view) {
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

    @OnClick(R.id.bt_nearby_cafe)
    public void openNearbyCafe(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_cafe));
    }

    private void startNearbyGridActivity(String type) {
        Intent intent = new Intent(this, NearByGridActivity.class);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_LATLNG, "51.5073509,-0.1277583");
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_RADIUS, 1000);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE, type);
        startActivity(intent);
    }

    /**
     * This method is called when Google place picker returns the result
     * @param requestCode The request code used for the place picker request
     * @param resultCode Result code of Google Place Picker intent
     * @param data Google Place picker Intent data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Timber.d(String.format("Place: %s", place.getName()));
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                List<Integer> placeTypes = place.getPlaceTypes();
                for(int type : placeTypes) {
                    Timber.d("Place Type -> " + type);
                }
                LatLng placeLatLng = place.getLatLng();
                Timber.d("Lat -> " + placeLatLng.latitude);
                Timber.d("Long -> " + placeLatLng.longitude);
            }
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        //First ask for permission
        getLocationPermission();

        try {
            if (mLocationPermissionGranted) {
                Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
                placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            Timber.i(String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                            Timber.e("Latitude-> " + Double.toString(placeLikelihood.getPlace().getLatLng().latitude));
                            Timber.e("Longitude-> " + Double.toString(placeLikelihood.getPlace().getLatLng().longitude));
                            Timber.e("Address -> " + placeLikelihood.getPlace().getAddress().toString());

                            Geocoder mGeocoder = new Geocoder(HomeActivity.this, Locale.getDefault());
                            try {
                                List<Address> addresses = mGeocoder.getFromLocation(placeLikelihood.getPlace().getLatLng().latitude,
                                placeLikelihood.getPlace().getLatLng().longitude, 1);
                                if (addresses != null && addresses.size() > 0) {
                                    Timber.e("Locality -> " + addresses.get(0).getLocality());
                                    Timber.e("Country -> " + addresses.get(0).getCountryName());
                                    Timber.e("PostalCode -> " + addresses.get(0).getPostalCode());
                                    Timber.e("Phone -> " + addresses.get(0).getPhone());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        likelyPlaces.release();
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
        //updateLocationUI();
    }
}
