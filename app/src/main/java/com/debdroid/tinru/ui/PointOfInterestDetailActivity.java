package com.debdroid.tinru.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.debdroid.tinru.R;
import com.debdroid.tinru.datamodel.GooglePlacesCustomPlaceDetailApi.GooglePlacesCustomPlaceDetailResponse;
import com.debdroid.tinru.utility.NetworkUtility;
import com.debdroid.tinru.viewmodel.NearbyGridViewModel;
import com.debdroid.tinru.viewmodel.PointOfInterestDetailViewModel;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class PointOfInterestDetailActivity extends AppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    Picasso picasso;

    @BindView(R.id.pb_poi_detail_activity) ProgressBar progressBar;
    @BindView(R.id.tv_poi_detail_pb_text_msg) TextView progressMsgTextView;
    @BindView(R.id.poi_detail_relative_layout) RelativeLayout relativeLayout;
    @BindView(R.id.iv_poi_detail_image) ImageView imageView;
    @BindView(R.id.tv_poi_detail_place_name) TextView nameTextView;
    @BindView(R.id.tv_poi_detail_place_address) TextView addressTextView;
    @BindView(R.id.tv_poi_detail_place_rating_Value) TextView ratingValueTextView;
    @BindView(R.id.rb_poi_detail_rating_bar) RatingBar ratingBar;
    @BindView(R.id.rb_poi_detail_user_review) TextView userReviewTextView;
    @BindView(R.id.bt_nearby_restaurant) Button nearbyButtonRestaurant;
    @BindView(R.id.bt_nearby_cafe) Button nearbyButtonCafe;
    @BindView(R.id.bt_nearby_bar) Button nearbyButtonBar;
    @BindView(R.id.bt_nearby_atm) Button nearbyButtonATM;
    @BindView(R.id.bt_nearby_shopping) Button nearbyButtonShopping;
    @BindView(R.id.bt_nearby_petrol_pump) Button nearbyButtonPetrolPump;

    private String placeId = "ChIJdd4hrwug2EcRmSrV3Vo6llI"; // Default place id of London
    private String location = "London"; // Default location - London
    private String address = "London, UK"; // Default address of London
    private double latitude = 51.509865; // Default latitude of London
    private double longitude = -0.118092; // Default longitude of London
    private double rating = 0.0; // Default rating
    private String photoReference = "CmRbAAAAaKRxb5Wh3d0IT66PwSJNFIGg4VZY8CtQiqOA27yUBpGJEhm29tT4TQA-g_LUyfhWWvVIwfWroj0eRug8M8_GAQNKqWjXp_TipLUx8RVDHgFYqDU8LlNIOfH7U2VUim_2EhC4Xxal6jHE9HPJAXVAe4Y9GhRnUb9UlgIySXNL12G-Z4Sx-F-6zg";

    public static final String EXTRA_POI_DETAIL_PLACE_ID = "extra_poi_detail_place_id";
    public static final String EXTRA_POI_DETAIL_LOCATION = "extra_poi_detail_location";
    public static final String EXTRA_POI_DETAIL_ADDRESS = "extra_poi_detail_address";
    public static final String EXTRA_POI_DETAIL_RATING = "extra_poi_detail_rating";
    public static final String EXTRA_POI_DETAIL_LATITUDE = "extra_poi_detail_latitude";
    public static final String EXTRA_POI_DETAIL_LONGITUDE = "extra_poi_detail_longitude";
    public static final String EXTRA_POI_DETAIL_PHOTO_REFERENCE = "extra_poi_detail_photo_reference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate is called");
        // Always Inject before super.onCreate, otherwise it fails while orientation change
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_interest_detail);
        ButterKnife.bind(this);

        // Get extras from the intent
        Intent intent = getIntent();
        if(intent.hasExtra(EXTRA_POI_DETAIL_PLACE_ID)) placeId = intent.getStringExtra(EXTRA_POI_DETAIL_PLACE_ID);
        if(intent.hasExtra(EXTRA_POI_DETAIL_LOCATION)) location = intent.getStringExtra(EXTRA_POI_DETAIL_LOCATION);
        if(intent.hasExtra(EXTRA_POI_DETAIL_ADDRESS)) address = intent.getStringExtra(EXTRA_POI_DETAIL_ADDRESS);
        latitude = intent.getDoubleExtra(EXTRA_POI_DETAIL_LATITUDE,0.0);
        longitude = intent.getDoubleExtra(EXTRA_POI_DETAIL_LONGITUDE,0.0);
        rating = intent.getDoubleExtra(EXTRA_POI_DETAIL_RATING,0.0);
        if(intent.hasExtra(EXTRA_POI_DETAIL_PHOTO_REFERENCE)) photoReference = intent.getStringExtra(EXTRA_POI_DETAIL_PHOTO_REFERENCE);

        // Set the action bar title
        setTitle(location);

        // Show the progress bar and hide the layout
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressMsgTextView.setVisibility(TextView.VISIBLE);
        relativeLayout.setVisibility(LinearLayout.INVISIBLE);

        // Create the ViewModel
        PointOfInterestDetailViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PointOfInterestDetailViewModel.class);
        viewModel.getPointOfInterestDetail(placeId, getString(R.string.google_maps_key)).observe(this,
                (pointOfInterestDetail) -> {
                    Timber.d("PointOfInterestDetailViewModel refreshed!!");
                    updateUi(pointOfInterestDetail);
                });

    }

    private void updateUi(GooglePlacesCustomPlaceDetailResponse detailResponse) {
        Timber.d("updateUi is called");
        // Hide the progressbar and associated text view
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        progressMsgTextView.setVisibility(TextView.INVISIBLE);
        // Show the Relative layout
        relativeLayout.setVisibility(RelativeLayout.VISIBLE);

        nameTextView.setText(location);
        addressTextView.setText(address);
        String formattedRatingValue = String.format("%.1f", (float)rating);
        ratingValueTextView.setText(formattedRatingValue);
        ratingBar.setRating((float)rating);
        if(detailResponse.getCustomPlaceDetailResult().getReviews() != null) {
            userReviewTextView.setText(detailResponse
                    .getCustomPlaceDetailResult().getReviews().get(0).getText());
        }

        Uri uri = Uri.parse(NetworkUtility.getGooglePlaceApiBaseUrl())
                .buildUpon()
                .path("maps/api/place/photo")
                .appendQueryParameter("maxwidth","600")
                .appendQueryParameter("key",getString(R.string.google_maps_key))
                .appendQueryParameter("photoreference", photoReference)
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
                    .into(imageView);
        }
    }

    @OnClick(R.id.bt_nearby_restaurant)
    public void openNearbyRestaurant(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_restaurant),
                getString(R.string.home_activity_layout_nearby_restaurant));
    }

    @OnClick(R.id.bt_nearby_cafe)
    public void openNearbyCafe(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_cafe),
                getString(R.string.home_activity_layout_nearby_cafe));
    }

    @OnClick(R.id.bt_nearby_bar)
    public void openNearbyBar(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_bar),
                getString(R.string.home_activity_layout_nearby_bar));
    }

    @OnClick(R.id.bt_nearby_atm)
    public void openNearbyATM(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_atm),
                getString(R.string.home_activity_layout_nearby_atm));
    }

    @OnClick(R.id.bt_nearby_shopping)
    public void openNearbyShopping(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_shopping),
                getString(R.string.home_activity_layout_nearby_shopping));
    }

    @OnClick(R.id.bt_nearby_petrol_pump)
    public void openNearbyPetrolPump(View view) {
        startNearbyGridActivity(getString(R.string.google_places_api_nearby_type_petrol_pump),
                getString(R.string.home_activity_layout_nearby_petrol_pump));
    }

    private void startNearbyGridActivity(String type, String typeName) {
        Intent intent = new Intent(this, NearByGridActivity.class);
        String latLng = String.format("%s,%s",latitude,longitude);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_LATLNG, latLng);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_RADIUS, 1000);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE, type);
        intent.putExtra(NearByGridActivity.EXTRA_NEARBY_TYPE_NAME, typeName);
        startActivity(intent);
    }
}
