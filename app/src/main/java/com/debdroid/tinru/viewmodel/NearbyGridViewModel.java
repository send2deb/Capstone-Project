package com.debdroid.tinru.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.debdroid.tinru.database.NearbyResultEntity;
import com.debdroid.tinru.datamodel.Result;
import com.debdroid.tinru.repository.TinruRepository;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class NearbyGridViewModel extends ViewModel {
    private LiveData<List<NearbyResultEntity>> nearbyResultEntityList;
    private TinruRepository tinruRepository;
    private String currentType;
    private String previousType;
    private boolean needFreshData = false;

    @Inject
    public NearbyGridViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<List<NearbyResultEntity>> getResultList(String latLng, int radius, String type, String apiKey) {
        Timber.d("getResultList is called");
        currentType = type;

        Timber.d("Previous type -> " + previousType);
        Timber.d("Current type -> " + currentType);

        // When type changes, set the nearbyResultEntityList to Null to force data load for new type
        if(!currentType.equals(previousType)) {
            Timber.d("Current type '" + currentType + "' not equal to previous type '" + previousType + "'");
            nearbyResultEntityList = null;
            previousType = currentType;
            needFreshData = true;
        } else {
            Timber.d("Current type '" + currentType + "' is equal to previous type '" + previousType + "'");
            needFreshData = false;
        }

        if(nearbyResultEntityList == null) {
            Timber.d("nearbyResultEntityList is null");
//            resultList = tinruRepository.getResult();
            nearbyResultEntityList = tinruRepository.getResult(latLng,radius,type,apiKey, needFreshData);
//            previousType = type;
        }
        return nearbyResultEntityList;
    }
}
