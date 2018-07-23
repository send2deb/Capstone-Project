package com.debdroid.tinru.dagger;

import com.debdroid.tinru.ui.appwidget.TinruListAppWidgetService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class TinruServiceModule {
    @TinruCustomScope.TinruServiceScope
    @ContributesAndroidInjector
    abstract TinruListAppWidgetService contributeTinruListAppWidgetServiceInjector();
}
