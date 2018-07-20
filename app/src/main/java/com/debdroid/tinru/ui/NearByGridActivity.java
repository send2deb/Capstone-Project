package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.adapter.NearbyGridAdapter;
import com.debdroid.tinru.viewmodel.NearbyGridViewModel;
import com.squareup.picasso.Picasso;

import java.sql.Time;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class NearByGridActivity extends AppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    Picasso picasso;
    @BindView(R.id.rv_nearby_item_grid)
    RecyclerView recyclerView;
    @BindDimen(R.dimen.single_nearby_item_image_width)
    float recipeCardWidth;

    private NearbyGridAdapter nearbyGridAdapter;
    private Parcelable linearLayoutManagerState;
    public static final String EXTRA_NEARBY_LATLNG = "extra_nearby_latlng";
    public static final String EXTRA_NEARBY_RADIUS = "extra_nearby_radius";
    public static final String EXTRA_NEARBY_TYPE = "extra_nearby_type";
    public static final String EXTRA_NEARBY_TYPE_NAME = "extra_nearby_type_name";
    private final String STATE_LINEAR_LAYOUT_MANAGER = "state_linear_layout_manager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_grid);
        ButterKnife.bind(this);

        // In case of orientation change, restore the layout manager state
        if (savedInstanceState != null) {
            linearLayoutManagerState = savedInstanceState.getParcelable(STATE_LINEAR_LAYOUT_MANAGER);
        }

        // Get extras from the intent
        Intent intent = getIntent();
        String latLng = "51.509865,-0.118092"; // Default latlng - London
        int radius = 500; // Default 500 meter
        String type = getString(R.string.google_places_api_nearby_type_restaurant); // Default type - restaurant
        String typeName = getString(R.string.home_activity_layout_nearby_restaurant); // Default type - restaurant
        String apiKey = getString(R.string.google_maps_key);

        if(intent.hasExtra(EXTRA_NEARBY_LATLNG)) latLng = intent.getStringExtra(EXTRA_NEARBY_LATLNG);
        if(intent.hasExtra(EXTRA_NEARBY_RADIUS)) radius = intent.getIntExtra(EXTRA_NEARBY_RADIUS, radius);
        if(intent.hasExtra(EXTRA_NEARBY_TYPE)) type = intent.getStringExtra(EXTRA_NEARBY_TYPE);
        if(intent.hasExtra(EXTRA_NEARBY_TYPE_NAME)) typeName = intent.getStringExtra(EXTRA_NEARBY_TYPE_NAME);

        // Set the action bar title
        setTitle(typeName);

        //Setup adapter and ViewModel
        nearbyGridAdapter = new NearbyGridAdapter(picasso, apiKey, vh -> {
            //TODO implement later
        });
//        if(isTabletMode) { // For table use the gridlayout
//            int spanCount = determineNumOfColumns();
//            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
//            recyclerView.setLayoutManager(gridLayoutManager);
//        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
//        }
        //Set this to false for smooth scrolling of RecyclerView
        recyclerView.setNestedScrollingEnabled(false);
        //Set this to false so that activity starts the page from the beginning
        recyclerView.setFocusable(false);
        // Set this to true for better performance (adapter content is fixed)
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(nearbyGridAdapter);
        // Create the ViewModel
        NearbyGridViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(NearbyGridViewModel.class);
        viewModel.getResultList(latLng, radius, type, apiKey).observe(this,
                (nearbyResultEntityList) -> {
                    Timber.d("viewModel refreshed!!");
                    nearbyGridAdapter.swapData(nearbyResultEntityList);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Timber.d("onBackPressed is called");
        // Destroy the activity so that ViewModle also gets destroyed for fresh data load next time
        finish();
    }

    /**
     * This method dynamically determines the number of column for the RecyclerView based on
     * screen width and density
     * @return number of calculated columns
     */
    private int determineNumOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Timber.d("displayMetrics.density -> " + displayMetrics.density);
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnWidth = (int) (recipeCardWidth / displayMetrics.density);
        int numOfColumn = (int) dpWidth / columnWidth;
        return numOfColumn;
    }
}
