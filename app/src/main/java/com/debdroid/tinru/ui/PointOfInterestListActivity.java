package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.adapter.PointOfInterestAdapter;
import com.debdroid.tinru.utility.CommonUtility;
import com.debdroid.tinru.utility.NetworkUtility;
import com.debdroid.tinru.viewmodel.PointOfInterestListViewModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import java.util.Timer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class PointOfInterestListActivity extends AppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    Picasso picasso;
    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.pb_poi_list_activity)
    ProgressBar progressBar;
    @BindView(R.id.tv_poi_list_pb_text_msg)
    TextView progressMsgTextView;
    @BindView(R.id.poi_list_relative_layout)
    RelativeLayout relativeLayout;
    @BindView(R.id.rv_poi_list)
    RecyclerView recyclerView;

    private PointOfInterestAdapter pointOfInterestAdapter;
    private Parcelable linearLayoutManagerState;
    private final String STATE_LINEAR_LAYOUT_MANAGER = "state_linear_layout_manager";
    private InterstitialAd mInterstitialAd;

    public static final String EXTRA_POINT_OF_INTEREST_LOCATION = "extra_point_of_interest_location";
    public static final String EXTRA_POINT_OF_INTEREST_AIRPORT_CODE = "extra_point_of_interest_airport_code";
    public static final String EXTRA_POINT_OF_INTEREST_IS_APPWIDGET_INTENT = "extra_point_of_interest_appwidget_intent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_interest_list);
        ButterKnife.bind(this);

        // If the device is not online then show a message and return
        // Use progress bar message to show no internet connection
        if(!NetworkUtility.isOnline(this)) {
            progressMsgTextView.setVisibility(TextView.VISIBLE);
            progressMsgTextView.setText(getString(R.string.no_network_error_msg));
            progressBar.setVisibility(ProgressBar.INVISIBLE); // Hide the progressbar
            relativeLayout.setVisibility(LinearLayout.INVISIBLE); // Hide the relative layout
            return;
        } else { // Make sure the message is replaced properly when device is online
            progressMsgTextView.setText(getString(R.string.home_progressbar_text_msg));
        }

        // In case of orientation change, restore the layout manager state
        if (savedInstanceState != null) {
            linearLayoutManagerState = savedInstanceState.getParcelable(STATE_LINEAR_LAYOUT_MANAGER);
        }

        // Get extras from the intent
        Intent intent = getIntent();
        String location = getString(R.string.default_destination_location_delhi); // Default location - Delhi
        String airportCode = getString(R.string.default_destination_airport_code_delhi); // Default code - DEL
        boolean isAppwidgetIntent = false; // Default - regular start
        String apiKey = getString(R.string.google_maps_key);

        if (intent.hasExtra(EXTRA_POINT_OF_INTEREST_LOCATION)) location =
                intent.getStringExtra(EXTRA_POINT_OF_INTEREST_LOCATION);
        if(intent.hasExtra(EXTRA_POINT_OF_INTEREST_AIRPORT_CODE)) airportCode =
                intent.getStringExtra(EXTRA_POINT_OF_INTEREST_AIRPORT_CODE);
        if(intent.hasExtra(EXTRA_POINT_OF_INTEREST_IS_APPWIDGET_INTENT)) isAppwidgetIntent =
                intent.getBooleanExtra(EXTRA_POINT_OF_INTEREST_IS_APPWIDGET_INTENT, false);


        // Set the action bar title
        setTitle(location);

        // If this activity is started by the widget then save the airport code and the location
        // (i.e. destination location) to sharedpreferenes in order to Flight list activity to
        // work properly. Note that origin city and airport code will always remain same for both
        // regular start of this activity or started by appwidget
        if(isAppwidgetIntent) {
            Timber.d("Activity started by appwidget");
            CommonUtility.saveDataInSharedPreference(sharedPreferences,
                    getString(R.string.preference_destination_airport_city_key), location);
            CommonUtility.saveDataInSharedPreference(sharedPreferences,
                    getString(R.string.preference_destination_airport_code_key), airportCode);
        } else {
            Timber.d("Activity started by Home - Regular start");
        }

        // Show the progress bar and hide the layout
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressMsgTextView.setVisibility(TextView.VISIBLE);
        relativeLayout.setVisibility(LinearLayout.INVISIBLE);

        // Initialize Ad
        initializeAd();
        //Setup adapter and ViewModel
        pointOfInterestAdapter = new PointOfInterestAdapter(this ,picasso, (placeId, name, address,
                                                                              latitude, longitude, rating,
                                                                              photoReference, vh) -> {
            startPointOfDetailActivity(placeId, name, address, latitude, longitude, rating, photoReference);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        //Set this to false for smooth scrolling of RecyclerView
        recyclerView.setNestedScrollingEnabled(false);
        //Set this to false so that activity starts the page from the beginning
        recyclerView.setFocusable(false);
        // Set this to true for better performance (adapter content is fixed)
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(pointOfInterestAdapter);
        // Create the ViewModel
        PointOfInterestListViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PointOfInterestListViewModel.class);
        // Create query parameter in the format "City point of interest"
        String locationPointOfInterest = location.concat(" ")
                .concat(getString(R.string.point_of_interest_query_parameter_part));
        viewModel.getPointOfInterestResultList(locationPointOfInterest, apiKey).observe(this,
                (pointOfInterestResultEntityList) -> {
                    Timber.d("PointOfInterestListViewModel refreshed!!");
                    if(pointOfInterestResultEntityList.isEmpty()) {
                        Timber.e("pointOfInterestResultEntityList is empty");
                    } else {
                        Timber.e("pointOfInterestResultEntityList is NOT empty");
                    }
                    pointOfInterestAdapter.swapData(pointOfInterestResultEntityList);
                    // Hide the progressbar and associated text view
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    progressMsgTextView.setVisibility(TextView.INVISIBLE);
                    // Show the Relative layout
                    relativeLayout.setVisibility(RelativeLayout.VISIBLE);
                    // Restore the position
                    if (linearLayoutManagerState != null) {
                        recyclerView.getLayoutManager().onRestoreInstanceState(linearLayoutManagerState);
                        // Set it to null so new value gets set
                        linearLayoutManagerState = null;
                    }
                });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if(recyclerView.getLayoutManager() != null) {
            linearLayoutManagerState = recyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(STATE_LINEAR_LAYOUT_MANAGER, linearLayoutManagerState);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Handle fab click action
     * @param view Associated view
     */
    @OnClick(R.id.fab_poi_list)
    public void fabAction(View view) {
        // Show the Interstitial Ads if it's ready. otherwise show a toast
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Timber.w("The interstitial ad wasn't loaded yet.");
            // Start the AsyncTask
            startFlightListActivity();
        }
    }

    /**
     * Start the point of interest detail activity
     * @param placeId Place id of the point of interest item
     * @param location Location of the point of interest
     * @param address Address of the point of interest item
     * @param latitude Latitude of the point of interest item
     * @param longitude Longitude of the point of interest item
     * @param rating Rating of the point of interest item
     * @param photoReference Photo reference of the point of interest item
     */
    private void startPointOfDetailActivity(String placeId, String location, String address, double latitude,
                                            double longitude, double rating, String photoReference) {
        Intent intent = new Intent(this, PointOfInterestDetailActivity.class);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_PLACE_ID, placeId);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_LOCATION, location);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_ADDRESS, address);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_LATITUDE, latitude);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_LONGITUDE, longitude);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_RATING, rating);
        intent.putExtra(PointOfInterestDetailActivity.EXTRA_POI_DETAIL_PHOTO_REFERENCE, photoReference);
        startActivity(intent);
    }

    private void  initializeAd() {
        // Initialize the MobAd
        MobileAds.initialize(this, getString(R.string.interstitial_ad_unit_id));

        // Create an interstitial ad object
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        // Load an ad
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        // Set the listener
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Start place of interest activity - start activity only after the airport code is retrieved
                startFlightListActivity();
            }
        });
    }

    /**
     * Start flight list activity
     */
    private void startFlightListActivity() {
        Intent intent = new Intent(this, FlightListActivity.class);
        startActivity(intent);
    }
}
