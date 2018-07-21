package com.debdroid.tinru.dagger;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.debdroid.tinru.viewmodel.NearbyGridViewModel;
import com.debdroid.tinru.viewmodel.PointOfInterestDetailViewModel;
import com.debdroid.tinru.viewmodel.PointOfInterestListViewModel;
import com.debdroid.tinru.viewmodel.TinruViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @TinruCustomScope.TinruApplicationScope
    abstract ViewModelProvider.Factory bindTinruViewModelFactory(TinruViewModelFactory factory);

    @Binds
    @IntoMap
    @ViewModelKey(NearbyGridViewModel.class)
    @TinruCustomScope.TinruApplicationScope
    abstract ViewModel bindNearbyGridViewModel(NearbyGridViewModel nearbyGridViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PointOfInterestListViewModel.class)
    @TinruCustomScope.TinruApplicationScope
    abstract ViewModel bindPointOfInterestListViewModel(PointOfInterestListViewModel pointOfInterestListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PointOfInterestDetailViewModel.class)
    @TinruCustomScope.TinruApplicationScope
    abstract ViewModel bindPointOfInterestDetailViewModel(PointOfInterestDetailViewModel pointOfInterestDetailViewModel);
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(RecipeIngredientViewModel.class)
//    @RecipeCustomScope.BakingApplicationScope
//    abstract ViewModel bindRecipeIngredientViewModel(RecipeIngredientViewModel recipeIngredientViewModel);
}
