package com.debdroid.tinru.utility;

import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.database.PointOfInterestResultEntity;
import com.debdroid.tinru.database.UserSearchedLocationEntity;
import com.debdroid.tinru.datamodel.GooglePlacesNearbyAPI.Result;
import com.debdroid.tinru.datamodel.GooglePlacesTextSearchApi.TextSearchResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepositoryUtility {

    /**
     * This method formats the nearby api data for database entity
     * @param resultList the nearby data to be formatted
     * @return formatted nearby api data for database
     */
    public static List<NearbyResultEntity> buildNearbyResultEntity(List<Result> resultList) {
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

    /**
     * This method formats the points of interest api data for database entity
     * @param textSearchResultList the points of interest data to be formatted
     * @return the formatted points of interest data for database
     */
    public static List<PointOfInterestResultEntity> buildPointOfInterestResultEntity
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

    /**
     * This method formats the user searched location data for database entity
     * @param location the location name
     * @param airportCode the airport code of the location
     * @param lat the latitude of the location
     * @param lng the longitude of the location
     * @param datetimestamp the system generated date and time stamp
     * @return the formatted location data for database
     */
    public static UserSearchedLocationEntity buildUserSearchedLocationEntity(String location, String airportCode,
                                                                             double lat, double lng,
                                                                             Date datetimestamp) {
        UserSearchedLocationEntity userSearchedLocationEntity = new UserSearchedLocationEntity();
        userSearchedLocationEntity.cityName = location;
        userSearchedLocationEntity.airportCode = airportCode;
        userSearchedLocationEntity.latitude = lat;
        userSearchedLocationEntity.longitude = lng;
        userSearchedLocationEntity.datetimestamp = datetimestamp;
        return userSearchedLocationEntity;
    }
}
