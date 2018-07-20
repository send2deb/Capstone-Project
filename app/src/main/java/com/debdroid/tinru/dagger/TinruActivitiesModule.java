package com.debdroid.tinru.dagger;

import com.debdroid.tinru.ui.FlightListActivity;
import com.debdroid.tinru.ui.HomeActivity;
import com.debdroid.tinru.ui.NearByGridActivity;
import com.debdroid.tinru.ui.PointOfInterestDetailActivity;
import com.debdroid.tinru.ui.PointOfInterestListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

//@Module(includes = TinruFragmentsModule.class)
@Module
abstract class TinruActivitiesModule {
    // This is the shortcut way, refer https://google.github.io/dagger/android
    @TinruCustomScope.HomeActivityScope
    @ContributesAndroidInjector
    abstract HomeActivity contributeTinruHomeActivityInjector();

    @TinruCustomScope.PointOfInterestListActivityScope
    @ContributesAndroidInjector
    abstract PointOfInterestListActivity contributePointOfInterestListActivityInjector();

    @TinruCustomScope.PointOfInterestDetailActivityScope
    @ContributesAndroidInjector
    abstract PointOfInterestDetailActivity contributePointOfInterestDetailActivityInjector();

    @TinruCustomScope.FlightListActivityScope
    @ContributesAndroidInjector
    abstract FlightListActivity contributeFlightListActivityInjector();

    @TinruCustomScope.NearByGridActivityScope
    @ContributesAndroidInjector
    abstract NearByGridActivity contributeNearByGridActivityInjector();
}
