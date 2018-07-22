package com.debdroid.tinru.utility;

import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.database.PointOfInterestResultEntity;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.Result;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.TextSearchResult;

import java.util.ArrayList;
import java.util.List;

public class RepositoryUtility {

    public static List<NearbyResultEntity> parseNearbyResultEntity(List<Result> resultList) {
        List<NearbyResultEntity> nearbyResultEntities = new ArrayList<>();
        for(Result result : resultList) {
            NearbyResultEntity nearbyResultEntity = new NearbyResultEntity();
            nearbyResultEntity.id = result.getId();
            nearbyResultEntity.nearbyName = result.getName();
            if(result.getGeometry() != null) {
                nearbyResultEntity.latitude = result.getGeometry().getLocation().getLat();
                nearbyResultEntity.longitude = result.getGeometry().getLocation().getLng();
            } else {
                nearbyResultEntity.latitude = 0.0;
                nearbyResultEntity.longitude = 0.0;
            }
            nearbyResultEntity.rating = result.getRating();
            if(result.getOpeningHours() != null) {
                if (result.getOpeningHours().getOpenNow()) {
                    nearbyResultEntity.openStatus = "Open Now";
                } else {
                    nearbyResultEntity.openStatus = "Closed Now";
                }
            } else {
                nearbyResultEntity.openStatus = "Open / Close data not available";
            }
            nearbyResultEntity.vicinity = result.getVicinity();
            if(result.getPhotos() != null) {
                nearbyResultEntity.photoReference = result.getPhotos().get(0).getPhotoReference();
            }
            nearbyResultEntities.add(nearbyResultEntity);
        }

        return nearbyResultEntities;
    }

    public static List<PointOfInterestResultEntity> parsePointOfInterestResultEntity
            (List<TextSearchResult> textSearchResultList) {
        List<PointOfInterestResultEntity> pointOfInterestResultEntities = new ArrayList<>();
        for(TextSearchResult textSearchResult : textSearchResultList) {
            PointOfInterestResultEntity pointOfInterestResultEntity = new PointOfInterestResultEntity();
            pointOfInterestResultEntity.id = textSearchResult.getId();
            pointOfInterestResultEntity.pointOfInterestName = textSearchResult.getName();
            if(textSearchResult.getGeometry() != null) {
                pointOfInterestResultEntity.latitude = textSearchResult.getGeometry().getLocation().getLat();
                pointOfInterestResultEntity.longitude = textSearchResult.getGeometry().getLocation().getLng();
            } else {
                pointOfInterestResultEntity.latitude = 0.0;
                pointOfInterestResultEntity.longitude = 0.0;
            }
            pointOfInterestResultEntity.formattedAddress = textSearchResult.getFormattedAddress();
            pointOfInterestResultEntity.placeId = textSearchResult.getPlaceId();
            pointOfInterestResultEntity.rating = textSearchResult.getRating();
            if(textSearchResult.getPhotos() != null) {
                pointOfInterestResultEntity.photoReference = textSearchResult.getPhotos().get(0).getPhotoReference();
            }
            // Add the individual point of interest item to the list for bulk insert
            pointOfInterestResultEntities.add(pointOfInterestResultEntity);
        }

        return pointOfInterestResultEntities;
    }
}
