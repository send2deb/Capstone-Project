package com.debdroid.tinru.dagger;

import com.debdroid.tinru.ui.appwidget.TinruAppWidgetProvider;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class TinruBroadcastModule {
    @TinruCustomScope.TinruBroadcastReceiverScope
    @ContributesAndroidInjector
    abstract TinruAppWidgetProvider contributeTinruAppWidgetProviderInjector();
}
