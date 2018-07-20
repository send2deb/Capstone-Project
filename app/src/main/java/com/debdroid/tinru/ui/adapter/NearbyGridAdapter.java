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
import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.datamodel.Result;
import com.debdroid.tinru.ui.NearByGridActivity;
import com.debdroid.tinru.utility.NetworkUtility;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
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

    public NearbyGridAdapter(Picasso picasso, String apiKey, NearbyAdapterOnClickHandler clickHandler) {
        Timber.d("NearbyGridAdapter constructor is called");
        nearbyGridAdapterOnClickHandler = clickHandler;
        this.picasso = picasso;
        this.apiKey = apiKey;
    }

    public class NearbyGridViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_single_nearby_image) ImageView nearbyImage;
        @BindView(R.id.tv_single_nearby_name) TextView nearbyName;
        @BindView(R.id.tv_single_nearby_rating) RatingBar nearbyRatingBar;
        @BindView(R.id.tv_single_nearby_open_close_status) TextView nearbyOpenCloseStatus;

        private NearbyGridViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                Timber.d("NearbyGrid item is clicked. Clicked item -> " + adapterPosition);
//                int recipeId = recipeEntityList.get(adapterPosition).id;
//                String recipeName = recipeEntityList.get(adapterPosition).name;
//                Timber.d("RecipeListViewHolder:Recipe id - "+recipeId);
//                Timber.d("RecipeListViewHolder:Recipe name - "+recipeName);
//                nearbyGridAdapterOnClickHandler.onRecipeItemClick(recipeId, recipeName,this);
            });
        }
    }

    @Override
    public NearbyGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_nearby_card_item,
                parent, false);
        return new NearbyGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyGridViewHolder holder, int position) {
        Timber.d("onBindViewHolder is called");
        String nearbyName = nearbyResultEntityList.get(position).nearbyName;
        float nearbyRating = (float) nearbyResultEntityList.get(position).rating;
        String nearbyOpenCloseStatus = nearbyResultEntityList.get(position).openStatus;
        String nearbyPhotoReference = nearbyResultEntityList.get(position).photoReference;

        if(nearbyName != null) holder.nearbyName.setText(nearbyName);
        holder.nearbyRatingBar.setRating(nearbyRating);
        holder.nearbyOpenCloseStatus.setText(nearbyOpenCloseStatus);

//        String apiKey = ((NearByGridActivity)nearbyGridAdapterOnClickHandler).getResources().getString(R.string.google_maps_key);
        Uri uri = Uri.parse(NetworkUtility.getGooglePlaceApiBaseUrl())
                .buildUpon()
                .path("maps/api/place/photo")
                .appendQueryParameter("maxwidth","400")
                .appendQueryParameter("key",apiKey)
                .appendQueryParameter("photoreference", nearbyPhotoReference)
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
//                    .placeholder(CommonUtility.getFallbackImageId(position))
//                    .error(CommonUtility.getFallbackImageId(position))
                    .into(holder.nearbyImage);
        }
    }

    @Override
    public int getItemCount() {
        if (nearbyResultEntityList.isEmpty()) {
            Timber.d("getItemCount is called, nearbyResultEntityList is empty");
            return 0;
        } else {
            Timber.d("getItemCount is called, nearbyResultEntityList count -> " + nearbyResultEntityList.size());
            return nearbyResultEntityList.size();
        }
    }

    public void swapData(List<NearbyResultEntity> nearbyResultEntityList) {
        Timber.d("swapData is called");
        if(nearbyResultEntityList != null) this.nearbyResultEntityList = nearbyResultEntityList;
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
        void onRecipeItemClick(NearbyAdapterOnClickHandler vh);
    }

}
