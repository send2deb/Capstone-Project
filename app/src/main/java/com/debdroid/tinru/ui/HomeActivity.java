package com.debdroid.tinru.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.debdroid.tinru.R;
import com.debdroid.tinru.repository.TinruRepository;
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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {
    @Inject
    TinruRepository tinruRepository;


    @BindView(R.id.tv_current_place_city_country) TextView currentCityCountryTextView;
    @BindView(R.id.bt_location_search) Button locationSearchButton;
    @BindView(R.id.pb_home_activity) ProgressBar progressBar;
    @BindView(R.id.tv_progressbar_text_msg) TextView progressBarTextMsg;
    @BindView(R.id.home_activity_linear_layout) LinearLayout linearLayout;
    @BindView(R.id.iv_current_place_image) ImageView currentPlaceImage;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    // The entry point to the Fused Location Provider.
//    private FusedLocationProviderClient mFusedLocationProviderClient;
    // A default location (London, UK) and default zoom to use when location permission is
    // not granted.
//    private final LatLng mDefaultLocation = new LatLng(51.5074, 0.1278);
//    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
//    private Location mLastKnownLocation;
    // Google place picker request number
    private int PLACE_PICKER_REQUEST = 1;

    private String currentPlaceName;
    private String currentPlaceId;
    private LatLng currentPlaceLatLng;
    private String currentPlaceCity;
    private String currentPlaceCountry;
    private String currentPostalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        tinruRepository.getPointOfInterestResult("london point of interest",
                getString(R.string.google_maps_key), true);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        // Construct a FusedLocationProviderClient.
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Show the progress bar and hide the layout
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressBarTextMsg.setVisibility(TextView.VISIBLE);
        linearLayout.setVisibility(LinearLayout.INVISIBLE);
        getDeviceLocation();

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

    @OnClick(R.id.bt_nearby_restaurant)
    public void openNearbyRestaurant(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_restaurant),
                getString(R.string.home_activity_layout_nearby_restaurant));
    }

    @OnClick(R.id.bt_nearby_cafe)
    public void openNearbyCafe(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_cafe),
                getString(R.string.home_activity_layout_nearby_cafe));
    }

    @OnClick(R.id.bt_nearby_bar)
    public void openNearbyBar(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_bar),
                getString(R.string.home_activity_layout_nearby_bar));
    }

    @OnClick(R.id.bt_nearby_atm)
    public void openNearbyATM(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_atm),
                getString(R.string.home_activity_layout_nearby_atm));
    }

    @OnClick(R.id.bt_nearby_shopping)
    public void openNearbyShopping(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_shopping),
                getString(R.string.home_activity_layout_nearby_shopping));
    }

    @OnClick(R.id.bt_nearby_petrol_pump)
    public void openNearbyPetrolPump(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_petrol_pump),
                getString(R.string.home_activity_layout_nearby_petrol_pump));
    }

    private void startNearbyGridActivity(String type, String typeName) {
        Intent intent = new Intent(this, NearByGridActivity.class);
        String latLng = String.format("%s,%s",currentPlaceLatLng.latitude,currentPlaceLatLng.longitude);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_LATLNG, latLng);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_RADIUS, 1000);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE, type);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE_NAME, typeName);
        startActivity(intent);
    }

    /**
     * This method is called when Google place picker returns the result
     * @param requestCode The request code used for the place picker request
     * @param resultCode CustomPlaceDetailResult code of Google Place Picker intent
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
                startPointOfInterestListActivity(placeLatLng.latitude, placeLatLng.longitude);

            }
        }
    }

    private void startPointOfInterestListActivity(double latitude, double longitude) {
        Address address = getAddressFromGeoCoder(latitude, longitude);
        String poiLocation = address.getLocality();
        Intent intent = new Intent(this, PointOfInterestListActivity.class);
        intent.putExtra(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_LOCATION, poiLocation);
        startActivity(intent);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
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
                Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
                placeResult.addOnCompleteListener(task -> {
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Timber.d(String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                        Timber.d("Latitude-> " + Double.toString(placeLikelihood.getPlace().getLatLng().latitude));
                        Timber.d("Longitude-> " + Double.toString(placeLikelihood.getPlace().getLatLng().longitude));
                        Timber.d("Address -> " + placeLikelihood.getPlace().getAddress().toString());

                        Geocoder mGeocoder = new Geocoder(HomeActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = mGeocoder.getFromLocation(placeLikelihood.getPlace().getLatLng().latitude,
                            placeLikelihood.getPlace().getLatLng().longitude, 1);
                            if (addresses != null && addresses.size() > 0) {
                                Timber.d("Locality -> " + addresses.get(0).getLocality());
                                Timber.d("Country -> " + addresses.get(0).getCountryName());
                                Timber.d("PostalCode -> " + addresses.get(0).getPostalCode());
                                Timber.d("Phone -> " + addresses.get(0).getPhone());
                            }
                        } catch (IOException e) {
                            Timber.e("PlaceDetectionClient error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    Place place = likelyPlaces.get(0).getPlace();
                    float placeLikelihoodValue = likelyPlaces.get(0).getLikelihood();
                    // Pick the place which has maximum likelihood value
                    for(PlaceLikelihood placeLikelihood : likelyPlaces) {
                        if(placeLikelihood.getLikelihood() > placeLikelihoodValue) {
                            place = placeLikelihood.getPlace();
                            placeLikelihoodValue = placeLikelihood.getLikelihood();
                        }
                    }
                    currentPlaceName = place.getName().toString();
                    currentPlaceId = place.getId();
                    currentPlaceLatLng = place.getLatLng();

                    Address address = getAddressFromGeoCoder(currentPlaceLatLng.latitude,
                            currentPlaceLatLng.longitude);
                    currentPlaceCity = address.getLocality();
                    currentPlaceCountry = address.getCountryName();
                    currentPostalCode = address.getPostalCode();
                    Timber.d(String.format("Place '%s' of '%s', '%s' , '%s' has likelihood: %g",
                            currentPlaceName, currentPlaceCity, currentPlaceCountry,
                            currentPostalCode, placeLikelihoodValue));

                    // Update the ui with the details
                    updateUi();

                    // Release the buffer to avoid memory leak
                    likelyPlaces.release();
                });
            }
        } catch (SecurityException e)  {
            Timber.e("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find out the locality, country and other details of the picked up place using Geocoder
     * @param latitude
     * @param longitude
     * @return
     */
    private Address getAddressFromGeoCoder(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,
                    longitude, 1);
            if (addresses.size() > 0){
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

    private void updateUi() {
        Timber.d("updateUi is called");
        // Load the photo of local place
        loadLocalPlacePhoto();
        // Set the current City & Country
        String currentPlaceCityCountry = currentPlaceName;
        if(currentPlaceCity != null && currentPlaceCountry != null) {
            currentPlaceCityCountry = currentPlaceCountry.concat(String.format("%s, %s, %s", currentPlaceName, currentPlaceCity,
                    currentPlaceCountry));
        } else if (currentPlaceCity == null && currentPlaceCountry != null) {
            currentPlaceCityCountry = currentPlaceCountry.concat(currentPlaceCountry);
        } else if(currentPlaceCity != null && currentPlaceCountry == null) {
            currentPlaceCityCountry = currentPlaceCountry.concat(currentPlaceCity);
        }
        currentCityCountryTextView.setText(currentPlaceCityCountry);

        // Hide the progress bar and show the layout
        progressBar.setVisibility(ProgressBar.GONE);
        progressBarTextMsg.setVisibility(TextView.GONE);
        linearLayout.setVisibility(LinearLayout.VISIBLE);
    }

    private void loadLocalPlacePhoto() {
        Timber.d(("loadLocalPlacePhoto is called. currentPlaceId -> " + currentPlaceId));
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(currentPlaceId);
        photoMetadataResponse.addOnCompleteListener(task -> {
            // Get the list of photos.
            PlacePhotoMetadataResponse photos = task.getResult();
            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
            // Get the first photo in the list.
            if(photoMetadataBuffer.getCount() > 0) {
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        currentPlaceImage.setImageBitmap(bitmap);
                    }
                });
                // Release the buffer to avoid memory leak
                photoMetadataBuffer.release();
            } else {
                Timber.d("No photo found for place id -> " + currentPlaceId);
            }
        });
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
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
