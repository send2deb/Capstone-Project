package com.debdroid.tinru.ui.appwidget;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.debdroid.tinru.R;
import com.debdroid.tinru.database.TinruDatabase;
import com.debdroid.tinru.database.UserSearchedLocationDao;
import com.debdroid.tinru.database.UserSearchedLocationEntity;
import com.debdroid.tinru.ui.PointOfInterestListActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.android.AndroidInjection;
import timber.log.Timber;

public class TinruListAppWidgetService extends RemoteViewsService {

    @Override
    public void onCreate() {
        Timber.d("onCreate is called");
        // Always call this before super
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TinruListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class TinruListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private UserSearchedLocationDao userSearchedLocationDao;
    private List<UserSearchedLocationEntity> userSearchedLocationEntityList = new ArrayList<>();
    private TinruDatabase tinruDatabase;

    public TinruListRemoteViewsFactory(Context context, Intent intent) {
        Timber.d("TinruListRemoteViewsFactory constructor is called");
        this.context = context;
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate is called");
        // Create database and get the dao
        // Here the room database is created to use on main thread
        tinruDatabase = Room.databaseBuilder(context.getApplicationContext(),
                TinruDatabase.class, context.getString(R.string.database_name)).allowMainThreadQueries().build();
        userSearchedLocationDao = tinruDatabase.getUserSearchedLocationDao();
    }

    @Override
    public void onDataSetChanged() {
        Timber.d("onDataSetChanged is called");
        // Get the data
        userSearchedLocationEntityList = getDataFromUserSearchedLocationTable();
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy is called");
        // Tear down
        userSearchedLocationEntityList.clear();
        tinruDatabase.close();
    }

    @Override
    public int getCount() {
        return userSearchedLocationEntityList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Timber.d("getViewAt is called. Position -> " + position);

        // Get the corresponding location from the list
        String location = userSearchedLocationEntityList.get(position).cityName;
        String airportCode = userSearchedLocationEntityList.get(position).airportCode;

        // Construct a remote views item based on our widget item xml file, and set the
        // text with location.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.tinru_single_appwidget_item);
        rv.setTextViewText(R.id.tv_appwidget_item, location);

        // Fill in the onClick PendingIntent Template using the specific location for each item individually
        Bundle extras = new Bundle();
        extras.putString(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_LOCATION, location);
        extras.putString(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_AIRPORT_CODE, airportCode);
        extras.putBoolean(PointOfInterestListActivity.EXTRA_POINT_OF_INTEREST_IS_APPWIDGET_INTENT, true);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.tv_appwidget_item, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    /**
     * This method retrieves the data from database on main thread!
     * This is okay as it's used by Widget only and as per developer
     * guideline (https://developer.android.com/guide/topics/appwidgets/) it is okay to do the heavy lifting
     * in either getViewAt() or onDataSetChanged()
     *
     * @return user searched locations as list
     */
    private List<UserSearchedLocationEntity> getDataFromUserSearchedLocationTable() {
        Timber.d("getDataFromUserSearchedLocationTable is called");
        return userSearchedLocationDao.loadCustomSearchedLocationEntity();
    }
}
