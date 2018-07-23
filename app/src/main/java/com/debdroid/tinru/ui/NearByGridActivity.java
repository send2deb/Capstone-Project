package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.adapter.NearbyGridAdapter;
import com.debdroid.tinru.utility.NetworkUtility;
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

    public static final String EXTRA_NEARBY_LOCATION = "extra_nearby_location";
    public static final String EXTRA_NEARBY_LATLNG = "extra_nearby_latlng";
    public static final String EXTRA_NEARBY_RADIUS = "extra_nearby_radius";
    public static final String EXTRA_NEARBY_TYPE = "extra_nearby_type";
    public static final String EXTRA_NEARBY_TYPE_NAME = "extra_nearby_type_name";
    private final String STATE_LINEAR_LAYOUT_MANAGER = "state_linear_layout_manager";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    Picasso picasso;

    @BindView(R.id.pb_nearby_list_activity)
    ProgressBar progressBar;
    @BindView(R.id.tv_nearby_list_pb_text_msg)
    TextView progressMsgTextView;
    @BindView(R.id.nearby_list_relative_layout)
    RelativeLayout relativeLayout;
    @BindView(R.id.rv_nearby_item_grid)
    RecyclerView recyclerView;
    @BindDimen(R.dimen.single_nearby_item_image_width)
    float recipeCardWidth;
    private NearbyGridAdapter nearbyGridAdapter;
    private Parcelable linearLayoutManagerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_grid);
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
        String location = getString(R.string.default_location_london); // Default location - London
        String latLng = getString(R.string.default_latlng_london); // Default latlng - London
        int radius = getResources().getInteger(R.integer.default_nearby_search_radius_meter); // Default 500 meter
        String type = getString(R.string.google_places_api_nearby_type_restaurant); // Default type - restaurant
        String typeName = getString(R.string.home_activity_layout_nearby_restaurant); // Default type - restaurant
        String apiKey = getString(R.string.google_maps_key);

        if (intent.hasExtra(EXTRA_NEARBY_LOCATION))
            location = intent.getStringExtra(EXTRA_NEARBY_LOCATION);
        if (intent.hasExtra(EXTRA_NEARBY_LATLNG))
            latLng = intent.getStringExtra(EXTRA_NEARBY_LATLNG);
        if (intent.hasExtra(EXTRA_NEARBY_RADIUS))
            radius = intent.getIntExtra(EXTRA_NEARBY_RADIUS, radius);
        if (intent.hasExtra(EXTRA_NEARBY_TYPE)) type = intent.getStringExtra(EXTRA_NEARBY_TYPE);
        if (intent.hasExtra(EXTRA_NEARBY_TYPE_NAME))
            typeName = intent.getStringExtra(EXTRA_NEARBY_TYPE_NAME);

        // Set the action bar title
        setTitle(typeName);

        // Enable up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show the progress bar and hide the layout
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressMsgTextView.setVisibility(TextView.VISIBLE);
        relativeLayout.setVisibility(LinearLayout.INVISIBLE);

        //Setup adapter and ViewModel
        nearbyGridAdapter = new NearbyGridAdapter(this, picasso, (name, latitude, longitude, vh) -> {
                    // Creates an Intent that will load the location in Google map
                    Uri gmmIntentUri = Uri.parse(String.format("geo:%g,%g?q=%s", latitude, longitude, name));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
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
        viewModel.getResultList(location, latLng, radius, type, apiKey).observe(this,
                (nearbyResultEntityList) -> {
                    Timber.d("NearbyGridViewModel refreshed!!");
                    nearbyGridAdapter.swapData(nearbyResultEntityList);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        switch (item.getItemId()) {
            case android.R.id.home:
                Timber.d("Actionbar up button is clicked");
                // Finish the activity
                finish();
                return true;
    }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        linearLayoutManagerState = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(STATE_LINEAR_LAYOUT_MANAGER, linearLayoutManagerState);
        super.onSaveInstanceState(outState);
    }

    /**
     * This method dynamically determines the number of column for the RecyclerView based on
     * screen width and density
     *
     * @return number of calculated columns
     */
    private int determineNumOfColumns() { // Not in use but will be used for landscape/tablet mode
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Timber.d("displayMetrics.density -> " + displayMetrics.density);
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnWidth = (int) (recipeCardWidth / displayMetrics.density);
        int numOfColumn = (int) dpWidth / columnWidth;
        return numOfColumn;
    }
}
