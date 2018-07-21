package com.debdroid.tinru.ui.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.database.PointOfInterestResultEntity;
import com.debdroid.tinru.utility.NetworkUtility;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PointOfInterestAdapter extends RecyclerView.Adapter<PointOfInterestAdapter.PointOfInterestViewHolder> {

    private List<PointOfInterestResultEntity> pointOfInterestResultEntityList = new ArrayList<>();
    private Picasso picasso;
    private PointOfInterestAdapterOnClickHandler pointOfInterestAdapterOnClickHandler;
    private String apiKey;

    public PointOfInterestAdapter(Picasso picasso, String apiKey, PointOfInterestAdapterOnClickHandler clickHandler) {
        Timber.d("PointOfInterestAdapter constructor is called");
        pointOfInterestAdapterOnClickHandler = clickHandler;
        this.picasso = picasso;
        this.apiKey = apiKey;
    }

    public class PointOfInterestViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_poi_single_item_image) ImageView pointOfInterestImage;
        @BindView(R.id.tv_poi_single_item_name) TextView pointOfInterestName;
        @BindView(R.id.tv_poi_single_item_address) TextView pointOfInterestAddress;
        @BindView(R.id.tv_poi_single_item_rating_value) TextView pointOfInterestRatingValue;
        @BindView(R.id.rb_poi_single_item_rating_bar) RatingBar pointOfInterestRatingBar;

        private PointOfInterestViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                Timber.d("Point of interest item is clicked. Clicked item -> " + adapterPosition);
                String placeId = pointOfInterestResultEntityList.get(adapterPosition).placeId;
                String name = pointOfInterestResultEntityList.get(adapterPosition).pointOfInterestName;
                String address = pointOfInterestResultEntityList.get(adapterPosition).formattedAddress;
                double latitude = pointOfInterestResultEntityList.get(adapterPosition).latitude;
                double longitude = pointOfInterestResultEntityList.get(adapterPosition).longitude;
                double rating = pointOfInterestResultEntityList.get(adapterPosition).rating;
                String photoReference = pointOfInterestResultEntityList.get(adapterPosition).photoReference;
                Timber.d("Latitude & Longitude of " + name + "-> " + latitude + " & " + longitude);
                pointOfInterestAdapterOnClickHandler.onPointOfInterestItemClick(placeId, name, address,
                        latitude, longitude, rating, photoReference, this);
            });
        }
    }

    @Override
    public PointOfInterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_point_of_interest_item,
                parent, false);
        return new PointOfInterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointOfInterestViewHolder holder, int position) {
        Timber.d("onBindViewHolder is called");
        String pointOfInterestName = pointOfInterestResultEntityList.get(position).pointOfInterestName;
        String pointOfInterestAddress = pointOfInterestResultEntityList.get(position).formattedAddress;
        float nearbyRating = (float) pointOfInterestResultEntityList.get(position).rating;
        String pointOfInterestRatingValue = String.format("%.1f", nearbyRating);
        String pointOfInterestPhotoReference = pointOfInterestResultEntityList.get(position).photoReference;

        if(pointOfInterestName != null) holder.pointOfInterestName.setText(pointOfInterestName);
        if(pointOfInterestAddress != null) holder.pointOfInterestAddress.setText(pointOfInterestAddress);
        holder.pointOfInterestRatingValue.setText(pointOfInterestRatingValue);
        holder.pointOfInterestRatingBar.setRating(nearbyRating);

        Uri uri = Uri.parse(NetworkUtility.getGooglePlaceApiBaseUrl())
                .buildUpon()
                .path("maps/api/place/photo")
                .appendQueryParameter("maxwidth","200")
                .appendQueryParameter("key",apiKey)
                .appendQueryParameter("photoreference", pointOfInterestPhotoReference)
                .build();

        String photoReferenceUrlString = null;

        try {
            URL url = new URL(uri.toString());
            photoReferenceUrlString = url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(photoReferenceUrlString == null || photoReferenceUrlString.isEmpty()) {
//            picasso.load(CommonUtility.getFallbackImageId(position)).into(holder.recipeImage);
            //TODO set fallback image
        } else {
            picasso.load(photoReferenceUrlString)
                    //TODO add placehodler and fallback later
//              .placeholder(CommonUtility.getFallbackImageId(position))
//              .error(CommonUtility.getFallbackImageId(position))
                    .into(holder.pointOfInterestImage);
        }
    }

    @Override
    public int getItemCount() {
        if (pointOfInterestResultEntityList.isEmpty()) {
            Timber.d("getItemCount is called, pointOfInterestResultEntityList is empty");
            return 0;
        } else {
            Timber.d("getItemCount is called, pointOfInterestResultEntityList count -> " + pointOfInterestResultEntityList.size());
            return pointOfInterestResultEntityList.size();
        }
    }

    public void swapData(List<PointOfInterestResultEntity> pointOfInterestResultEntityList) {
        Timber.d("swapData is called");
        if(pointOfInterestResultEntityList != null) this.pointOfInterestResultEntityList = pointOfInterestResultEntityList;
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        pointOfInterestAdapterOnClickHandler = null;
    }

    /**
     * This is the interface which will be implemented by HostActivity
     */
    public interface PointOfInterestAdapterOnClickHandler {
        void onPointOfInterestItemClick(String placeId, String name, String address, double latitude,
                  double longitude, double rating, String photoReference, PointOfInterestViewHolder vh);
    }

}
