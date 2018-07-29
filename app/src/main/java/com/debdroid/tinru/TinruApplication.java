package com.debdroid.tinru;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;

import com.debdroid.tinru.dagger.DaggerTinruApplicationComponent;
import com.facebook.stetho.Stetho;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;
import timber.log.Timber;

public class TinruApplication extends Application implements HasActivityInjector,
        HasBroadcastReceiverInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingBroadcastInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    public static TinruApplication get(Context context) {
        return (TinruApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Plant Timber tree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        //Create Dagger application component
        DaggerTinruApplicationComponent.builder()
                .application(this)
                .build()
                .inject(this);

        //Initialize Stetho
        Stetho.initializeWithDefaults(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingBroadcastInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
