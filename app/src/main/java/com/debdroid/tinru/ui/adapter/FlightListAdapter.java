package com.debdroid.tinru.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.datamodel.AmadeusSandboxLowFareSearchApi.AmadeusSandboxLowFareSearchResponse;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FlightListAdapter extends RecyclerView.Adapter<FlightListAdapter.FlightListViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NON_EMPTY = 1;
    private AmadeusSandboxLowFareSearchResponse amadeusSandboxLowFareSearchResponse;
    private FlightListAdapterOnClickHandler flightListAdapterOnClickHandler;

    public FlightListAdapter(FlightListAdapterOnClickHandler clickHandler) {
        Timber.d("FlightListAdapter constructor is called");
        flightListAdapterOnClickHandler = clickHandler;
    }

    @Override
    public FlightListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view;
        if (viewType == VIEW_TYPE_NON_EMPTY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_flight_item,
                    parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_message_item,
                    parent, false);
        }
        return new FlightListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightListViewHolder holder, int position) {
        Timber.d("onBindViewHolder is called. Position -> " + position);

        if (holder.getItemViewType() == VIEW_TYPE_NON_EMPTY) {
            String currency = amadeusSandboxLowFareSearchResponse.getCurrency();
            // We retrieve only direct flight, so getFlights() will always have 1 item (i.e. used 0 as index)
            // But for direct flight, getItineraries can have multiple values; for simplicity we just take
            // the first itinerary (i.e. used 0 as index)
            String originCityCode = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getFlights().get(0).getOrigin().getAirport();
            String destinationCityCode = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getFlights().get(0).getDestination().getAirport();
            String airline = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getFlights().get(0).getOperatingAirline();
            String flightNumber = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getFlights().get(0).getFlightNumber();
            String departure = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getFlights().get(0).getDepartsAt();
            String arrival = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getFlights().get(0).getArrivesAt();
            String duration = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getItineraries().get(0).getOutbound().getDuration();

            String fare = amadeusSandboxLowFareSearchResponse.getResults()
                    .get(position).getFare().getTotalPrice();

            String displayFare = currency.concat(" ").concat(fare);
            String displayFlightNumber = airline.concat(" ").concat(flightNumber);
            String displayDeparture = departure.replace("T", " ");
            String displayArrival = arrival.replace("T", " ");

            holder.originCityCodeTextView.setText(originCityCode);
            holder.destinationCityCodeTextView.setText(destinationCityCode);
            holder.flightNumber.setText(displayFlightNumber);
            holder.departureTextView.setText(displayDeparture);
            holder.arrivalTextView.setText(displayArrival);
            holder.durationTextView.setText(duration);
            holder.fareTextView.setText(displayFare);
        } else {
            // Do nothing for empty view
        }
    }

    @Override
    public int getItemCount() {
        if (amadeusSandboxLowFareSearchResponse == null) {
            return 1; // Return 1 instead of 0 as we are using an empty view
        } else {
            // Since we use single itinerary and flight (when non-stop), so size of getResults() is what
            // we need but in case we want to use all itineraries or multiple flight then this needs change
            return amadeusSandboxLowFareSearchResponse.getResults().size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (amadeusSandboxLowFareSearchResponse == null) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_NON_EMPTY;
        }
    }

    public void swapData(AmadeusSandboxLowFareSearchResponse amadeusSandboxLowFareSearchResponse) {
        Timber.d("swapData is called");
        if (amadeusSandboxLowFareSearchResponse != null)
            this.amadeusSandboxLowFareSearchResponse = amadeusSandboxLowFareSearchResponse;
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        flightListAdapterOnClickHandler = null;
    }

    /**
     * This is the interface which will be implemented by HostActivity
     */
    public interface FlightListAdapterOnClickHandler {
        void onFlightListItemClick(FlightListViewHolder vh);
    }

    public class FlightListViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.tv_single_item_origin_city_code)
        TextView originCityCodeTextView;
        @Nullable
        @BindView(R.id.tv_single_item_destination_city_code)
        TextView destinationCityCodeTextView;
        @Nullable
        @BindView(R.id.tv_single_item_departure_value)
        TextView departureTextView;
        @Nullable
        @BindView(R.id.tv_single_item_arrival_value)
        TextView arrivalTextView;
        @Nullable
        @BindView(R.id.tv_single_item_duration_value)
        TextView durationTextView;
        @Nullable
        @BindView(R.id.tv_single_item_flight_fare_value)
        TextView fareTextView;
        @Nullable
        @BindView(R.id.tv_single_item_flight_number)
        TextView flightNumber;

        private FlightListViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                Timber.d("Flight item is clicked. Clicked item -> " + adapterPosition);
                flightListAdapterOnClickHandler.onFlightListItemClick(this);
            });
        }
    }

}