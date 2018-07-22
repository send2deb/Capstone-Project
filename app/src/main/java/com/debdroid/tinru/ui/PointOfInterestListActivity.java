package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.adapter.PointOfInterestAdapter;
import com.debdroid.tinru.viewmodel.PointOfInterestListViewModel;
import com.squareup.picasso.Picasso;

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

    @BindView(R.id.rv_poi_list)
    RecyclerView recyclerView;

    private PointOfInterestAdapter pointOfInterestAdapter;
    private Parcelable linearLayoutManagerState;
    private final String STATE_LINEAR_LAYOUT_MANAGER = "state_linear_layout_manager";

    public static final String EXTRA_POINT_OF_INTEREST_LOCATION = "extra_point_of_interest_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_interest_list);
        ButterKnife.bind(this);

        // In case of orientation change, restore the layout manager state
        if (savedInstanceState != null) {
            linearLayoutManagerState = savedInstanceState.getParcelable(STATE_LINEAR_LAYOUT_MANAGER);
        }

        // Get extras from the intent
        Intent intent = getIntent();
        String location = getString(R.string.default_location_london); // Default location - London
        String apiKey = getString(R.string.google_maps_key);

        if (intent.hasExtra(EXTRA_POINT_OF_INTEREST_LOCATION)) location =
                intent.getStringExtra(EXTRA_POINT_OF_INTEREST_LOCATION);

        // Set the action bar title
        setTitle(location);

        //Setup adapter and ViewModel
        pointOfInterestAdapter = new PointOfInterestAdapter(this ,picasso, apiKey, (placeId, name, address,
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
                    pointOfInterestAdapter.swapData(pointOfInterestResultEntityList);
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
        linearLayoutManagerState = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(STATE_LINEAR_LAYOUT_MANAGER, linearLayoutManagerState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Handle fab click action
     * @param view Associated view
     */
    @OnClick(R.id.fab_poi_list)
    public void fabAction(View view) {
        startFlightListActivity();
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

    /**
     * Start flight list activity
     */
    private void startFlightListActivity() {
        Intent intent = new Intent(this, FlightListActivity.class);
        startActivity(intent);
    }
}
