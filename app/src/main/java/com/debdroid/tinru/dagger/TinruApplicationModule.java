package com.debdroid.tinru.dagger;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;

import com.debdroid.tinru.R;
import com.debdroid.tinru.database.NearbyResultDao;
import com.debdroid.tinru.database.PointOfInterestResultDao;
import com.debdroid.tinru.database.TinruDatabase;
import com.debdroid.tinru.database.UserSearchedLocationDao;
import com.debdroid.tinru.retrofit.AmadeusSandboxPointOfInterestApiService;
import com.debdroid.tinru.retrofit.GooglePlacesApiService;
import com.debdroid.tinru.utility.NetworkUtility;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module(includes = ViewModelModule.class)
//@Module
class TinruApplicationModule {

    // Amadeus Sandbox Point Of Interest Json data api service using Retrofit
    @Provides
    @TinruCustomScope.TinruApplicationScope
    AmadeusSandboxPointOfInterestApiService provideAmadeusSandboxPointOfInterestApiService(Retrofit retrofit) {
        return retrofit.create(AmadeusSandboxPointOfInterestApiService.class);
    }

    // Google Place Json data api service using Retrofit
    @Provides
    @TinruCustomScope.TinruApplicationScope
    GooglePlacesApiService provideGooglePlacesNearbySearchApiService(Retrofit retrofit) {
        return retrofit.create(GooglePlacesApiService.class);
    }

    // Retrofit
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(NetworkUtility.getGooglePlaceApiBaseUrl())
//                .baseUrl(NetworkUtility.getAmadeusSandboxBaseUrl())
                .client(okHttpClient)
//                .client(NetworkUtility.getUnsafeOkHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                //execute call back in background thread
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .build();
    }

    // OkHttp client
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static OkHttpClient provideOkHttpClient(Cache cache, HttpLoggingInterceptor interceptor,
                                            StethoInterceptor stethoInterceptor) {
        return new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addNetworkInterceptor(stethoInterceptor)
                .build();
    }

    // OkHttp logging Interceptor
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> Timber.i(message));
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return interceptor;
    }

    // Stetho Interceptor
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static StethoInterceptor provideStethoInterceptor() {
        StethoInterceptor stethoInterceptor = new StethoInterceptor();
        return stethoInterceptor;
    }

    // Cache
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static Cache provideCache(File cacheFile) {
        int cacheSize = 100 * 1024 * 1024; //Create a cache of 100 MB
        return new Cache(cacheFile, cacheSize);
    }

    // File
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static File provideFile(Application app) {
        return new File(app.getCacheDir(), app.getResources().getString(R.string.okhttp_cache_name));
    }

    // Picasso
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static Picasso providePicasso(Application application, OkHttp3Downloader okHttp3Downloader) {
        return new Picasso.Builder(application)
                //Force Picasso to use the same OkHttp client used by Retrofit
                .downloader(okHttp3Downloader)
                .build();
    }

    // OkHttp downloader
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static OkHttp3Downloader provideOkHttp3Downloader(OkHttpClient okHttpClient) {
        return new OkHttp3Downloader(okHttpClient);
    }

    // TinruDatabase
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static TinruDatabase provideTinruDatabase(Application app) {
        return Room.databaseBuilder(app, TinruDatabase.class,
                app.getResources().getString(R.string.database_name))
                .build();
    }

    // UserSearchedLocationDao
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static UserSearchedLocationDao provideUserSearchedLocationDao(TinruDatabase TinruDatabase) {
        return TinruDatabase.getUserSearchedLocationDao();
    }

    // NearbyResultDao
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static NearbyResultDao provideNearbyResultDao(TinruDatabase TinruDatabase) {
        return TinruDatabase.getNearbyResultDao();
    }

    // PointOfInterestResultDao
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static PointOfInterestResultDao providePointOfInterestResultDao(TinruDatabase TinruDatabase) {
        return TinruDatabase.getPointOfInterestResultDao();
    }

    // SharedPreference
    @Provides
    @TinruCustomScope.TinruApplicationScope
    static SharedPreferences provideSharedPreferences(Application app) {
        return app.getSharedPreferences(app.getResources().getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
    }
}
