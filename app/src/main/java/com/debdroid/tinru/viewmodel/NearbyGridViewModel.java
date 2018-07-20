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

    @Inject
    public NearbyGridViewModel(TinruRepository tinruRepository) {
        this.tinruRepository = tinruRepository;
    }

    public LiveData<List<NearbyResultEntity>> getResultList(String latLng, int radius, String type, String apiKey) {
        Timber.d("getResultList is called");
        if(nearbyResultEntityList == null) {
//            resultList = tinruRepository.getResult();
            nearbyResultEntityList = tinruRepository.getResult(latLng,radius,type,apiKey);
        }
        return nearbyResultEntityList;
    }
}
