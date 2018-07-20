package com.debdroid.tinru.dagger;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.debdroid.tinru.viewmodel.NearbyGridViewModel;
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

//    @Binds
//    @IntoMap
//    @ViewModelKey(RecipeDetailViewModel.class)
//    @RecipeCustomScope.BakingApplicationScope
//    abstract ViewModel bindRecipeDetailViewModel(RecipeDetailViewModel recipeDetailViewModel);
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(RecipeStepDetailViewModel.class)
//    @RecipeCustomScope.BakingApplicationScope
//    abstract ViewModel bindRecipeStepDetailViewModel(RecipeStepDetailViewModel recipeStepDetailViewModel);
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(RecipeIngredientViewModel.class)
//    @RecipeCustomScope.BakingApplicationScope
//    abstract ViewModel bindRecipeIngredientViewModel(RecipeIngredientViewModel recipeIngredientViewModel);
}
