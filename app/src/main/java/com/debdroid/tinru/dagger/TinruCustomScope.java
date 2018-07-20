package com.debdroid.tinru.dagger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public @interface TinruCustomScope {
    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface TinruApplicationScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface HomeActivityScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface PointOfInterestListActivityScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface PointOfInterestDetailActivityScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface FlightListActivityScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface NearByGridActivityScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface TinruBroadcastReceiverScope {
    }

    @Scope
    @Documented
    @Retention(RUNTIME)
    @interface TinruServiceScope {
    }
}
