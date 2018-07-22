package com.debdroid.tinru.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.utility.CommonUtility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class NearbyGridAdapter extends RecyclerView.Adapter<NearbyGridAdapter.NearbyGridViewHolder> {

    private List<NearbyResultEntity> nearbyResultEntityList = new ArrayList<>();
    private Picasso picasso;
    private NearbyAdapterOnClickHandler nearbyGridAdapterOnClickHandler;
    private String apiKey;
    private Context context;

    public NearbyGridAdapter(Context context, Picasso picasso, String apiKey,
                             NearbyAdapterOnClickHandler clickHandler) {
        Timber.d("NearbyGridAdapter constructor is called");
        this.context = context;
        this.picasso = picasso;
        this.apiKey = apiKey;
        nearbyGridAdapterOnClickHandler = clickHandler;
    }

    @Override
    public NearbyGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_nearby_card_item,
                parent, false);
        return new NearbyGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyGridViewHolder holder, int position) {
        Timber.d("onBindViewHolder is called. Position -> " + position);
        String nearbyName = nearbyResultEntityList.get(position).nearbyName;
        String nearbyVicinity = nearbyResultEntityList.get(position).vicinity;
        float nearbyRating = (float) nearbyResultEntityList.get(position).rating;
        String nearbyRatingValue = String.format("%.1f", nearbyRating);
        String nearbyOpenCloseStatus = nearbyResultEntityList.get(position).openStatus;
        String nearbyPhotoReference = nearbyResultEntityList.get(position).photoReference;

        if (nearbyName != null) holder.nearbyName.setText(nearbyName);
        if (nearbyVicinity != null) holder.nearbyVicinity.setText(nearbyVicinity);
        holder.nearbyRatingValue.setText(nearbyRatingValue);
        holder.nearbyRatingBar.setRating(nearbyRating);
        holder.nearbyOpenCloseStatus.setText(nearbyOpenCloseStatus);

        String photoReferenceUrlString = CommonUtility.buildGooglePlacesPhotoUrl(context,
                Integer.toString(context.getResources().getInteger(R.integer.nearby_photo_download_width_size)),
                nearbyPhotoReference);
        if (photoReferenceUrlString == null || photoReferenceUrlString.isEmpty()) {
//            picasso.load(CommonUtility.getFallbackImageId(position)).into(holder.recipeImage);
            //TODO set fallback image
        } else {
            picasso.load(photoReferenceUrlString)
                    //TODO add placehodler and fallback later
//              .placeholder(CommonUtility.getFallbackImageId(position))
//              .error(CommonUtility.getFallbackImageId(position))
                    .into(holder.nearbyImage);
        }
    }

    @Override
    public int getItemCount() {
        if (nearbyResultEntityList.isEmpty()) {
            return 0;
        } else {
            return nearbyResultEntityList.size();
        }
    }

    public void swapData(List<NearbyResultEntity> nearbyResultEntityList) {
        Timber.d("swapData is called");
        if (nearbyResultEntityList != null) this.nearbyResultEntityList = nearbyResultEntityList;
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        nearbyGridAdapterOnClickHandler = null;
    }

    /**
     * This is the interface which will be implemented by HostActivity
     */
    public interface NearbyAdapterOnClickHandler {
        void onNearbyItemClick(String name, double latitude, double longitude, NearbyGridViewHolder vh);
    }

    public class NearbyGridViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_single_nearby_image)
        ImageView nearbyImage;
        @BindView(R.id.tv_single_nearby_name)
        TextView nearbyName;
        @BindView(R.id.tv_single_nearby_vicinity)
        TextView nearbyVicinity;
        @BindView(R.id.tv_single_nearby_rating_value)
        TextView nearbyRatingValue;
        @BindView(R.id.rb_single_nearby_rating)
        RatingBar nearbyRatingBar;
        @BindView(R.id.tv_single_nearby_open_close_status)
        TextView nearbyOpenCloseStatus;

        private NearbyGridViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                Timber.d("NearbyGrid item is clicked. Clicked item -> " + adapterPosition);
                String name = nearbyResultEntityList.get(adapterPosition).nearbyName;
                double latitude = nearbyResultEntityList.get(adapterPosition).latitude;
                double longitude = nearbyResultEntityList.get(adapterPosition).longitude;
                Timber.d("Latitude & Longitude of" + "name" + "-> " + latitude + " & " + longitude);
                nearbyGridAdapterOnClickHandler.onNearbyItemClick(name, latitude, longitude, this);
            });
        }
    }

}
