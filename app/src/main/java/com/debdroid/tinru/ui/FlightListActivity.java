package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.adapter.FlightListAdapter;
import com.debdroid.tinru.utility.CommonUtility;
import com.debdroid.tinru.utility.NetworkUtility;
import com.debdroid.tinru.viewmodel.FlightListViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class FlightListActivity extends AppCompatActivity {

    private final String STATE_LINEAR_LAYOUT_MANAGER = "state_linear_layout_manager";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.pb_flight_list_activity)
    ProgressBar progressBar;
    @BindView(R.id.tv_flight_list_pb_text_msg)
    TextView progressMsgTextView;
    @BindView(R.id.flight_list_relative_layout)
    RelativeLayout relativeLayout;
    @BindView(R.id.tv_flight_list_orig_to_destination_value)
    TextView originToDestinationTextView;
    @BindView(R.id.rv_flight_list)
    RecyclerView recyclerView;
    @BindView(R.id.flight_list_toolbar)
    Toolbar toolbar;

    private FlightListAdapter flightListAdapter;
    private Parcelable linearLayoutManagerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

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

        // Retrieve data from SharedPreference
        String originAirportCity = sharedPreferences.getString(getString(R.string.preference_origin_airport_city_key),
                getString(R.string.default_location_london)); // Default value London
        String destinationAirportCity = sharedPreferences.getString(getString(R.string.preference_destination_airport_city_key),
                getString(R.string.default_destination_location_delhi)); // Default value Delhi
        String originAirportCode = sharedPreferences.getString(getString(R.string.preference_origin_airport_code_key),
                getString(R.string.default_origin_airport_code_london)); // Default value LHR (London Heathrow)
        String destinationAirportCode = sharedPreferences.getString(getString(R.string.preference_destination_airport_code_key),
                getString(R.string.default_destination_airport_code_delhi)); // Default value DEL (Delhi)

        Timber.d("originAirportCity -> " + originAirportCity);
        Timber.d("destinationAirportCity -> " + destinationAirportCity);
        Timber.d("originAirportCode -> " + originAirportCode);
        Timber.d("destinationAirportCode -> " + destinationAirportCode);

        // Set the action bar title
//        setTitle(getString(R.string.flight_list_activity_title_prefix).concat(" ").concat(destinationAirportCity));
        getSupportActionBar().setTitle
                (getString(R.string.flight_list_activity_title_prefix).concat(" ").concat(destinationAirportCity));
        // Enable up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the flight from to destination view
        originToDestinationTextView.setText(getString(R.string.flight_list_orig_to_dest_msg,
                originAirportCity, destinationAirportCity));

        // Show the progress bar and hide the layout
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressMsgTextView.setVisibility(TextView.VISIBLE);
        relativeLayout.setVisibility(LinearLayout.INVISIBLE);

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
        Timber.d("getAmadeusLowFareFlightSearchDateFormat: " + CommonUtility.getAmadeusLowFareFlightSearchDateFormat());
        viewModel.getFlightData(originAirportCode, destinationAirportCode,
                getResources().getBoolean(R.bool.amadeus_flight_search_non_stop_flag),
                CommonUtility.getAmadeusLowFareFlightSearchDateFormat(), getString(R.string.amadeus_sandbox_key)).observe(this,
                (amadeusSandboxLowFareSearchResponse) -> {
                    Timber.d("FlightListViewModel refreshed!!");
                    if(amadeusSandboxLowFareSearchResponse == null) {
                        Timber.e("amadeusSandboxLowFareSearchResponse is null");
                    } else {
                        Timber.e("amadeusSandboxLowFareSearchResponse NOT null");
                    }
                    flightListAdapter.swapData(amadeusSandboxLowFareSearchResponse);
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
}
