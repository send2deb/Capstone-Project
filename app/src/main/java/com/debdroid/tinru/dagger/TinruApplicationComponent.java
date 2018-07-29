package com.debdroid.tinru.dagger;

import android.app.Application;

import com.debdroid.tinru.TinruApplication;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@TinruCustomScope.TinruApplicationScope
@Component(modules = {
        /* Use AndroidInjectionModule.class if you're not using support library */
        AndroidSupportInjectionModule.class,
        /* The application module */
        TinruApplicationModule.class,
        /* The module for all the activities */
        TinruActivitiesModule.class,
        /* The module for widget provider which is a broadcast receiver */
        TinruBroadcastModule.class,
        /* The module for service used for widget */
        TinruServiceModule.class
})
public interface TinruApplicationComponent {
    void inject(TinruApplication app);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        TinruApplicationComponent build();
    }
}
