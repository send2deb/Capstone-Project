package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.adapter.FlightListAdapter;
import com.debdroid.tinru.ui.adapter.PointOfInterestAdapter;
import com.debdroid.tinru.viewmodel.FlightListViewModel;
import com.debdroid.tinru.viewmodel.PointOfInterestListViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class FlightListActivity extends AppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.tv_flight_list_orig_to_destination_value)
    TextView originToDestinationTextView;
    @BindView(R.id.rv_flight_list)
    RecyclerView recyclerView;

    private FlightListAdapter flightListAdapter;
    private Parcelable linearLayoutManagerState;
    private final String STATE_LINEAR_LAYOUT_MANAGER = "state_linear_layout_manager";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);
        ButterKnife.bind(this);

        // In case of orientation change, restore the layout manager state
        if (savedInstanceState != null) {
            linearLayoutManagerState = savedInstanceState.getParcelable(STATE_LINEAR_LAYOUT_MANAGER);
        }

        // Retrieve data from SharedPreference
        String originAirportCity = sharedPreferences.getString(getString(R.string.preference_origin_airport_city_key),
                "London"); // Default value London
        String destinationAirportCity = sharedPreferences.getString(getString(R.string.preference_destination_airport_city_key),
                "Delhi"); // Default value Delhi
        String originAirportCode = sharedPreferences.getString(getString(R.string.preference_origin_airport_code_key),
                "LHR"); // Default value LHR (London Heathrow)
        String destinationAirportCode = sharedPreferences.getString(getString(R.string.preference_destination_airport_code_key),
                "DEL"); // Default value DEL (Delhi)

        Timber.d("originAirportCity -> " + originAirportCity);
        Timber.d("destinationAirportCity -> " + destinationAirportCity);
        Timber.d("originAirportCode -> " + originAirportCode);
        Timber.d("destinationAirportCode -> " + destinationAirportCode);

        // Set the action bar title
        setTitle(getString(R.string.flight_list_activity_title_prefix).concat(" ").concat(destinationAirportCity));

        // Set the flight from to destination view
        originToDestinationTextView.setText(originAirportCity.concat(" to ").concat(destinationAirportCity));

        //Setup adapter and ViewModel
        flightListAdapter = new FlightListAdapter(vh -> {
            // Currently no action is done on flight item click
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
        recyclerView.setAdapter(flightListAdapter);
        // Create the ViewModel
        FlightListViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(FlightListViewModel.class);
        viewModel.getFlightData(originAirportCode, destinationAirportCode, true,
                "2018-07-23", getString(R.string.amadeus_sandbox_key)).observe(this,
                (amadeusSandboxLowFareSearchResponse) -> {
                    Timber.d("FlightListViewModel refreshed!!");
                    flightListAdapter.swapData(amadeusSandboxLowFareSearchResponse);
                    // Restore the position
                    if (linearLayoutManagerState != null) {
                        recyclerView.getLayoutManager().onRestoreInstanceState(linearLayoutManagerState);
                        // Set it to null so new value gets set
                        linearLayoutManagerState = null;
                    }
                });
    }
}
