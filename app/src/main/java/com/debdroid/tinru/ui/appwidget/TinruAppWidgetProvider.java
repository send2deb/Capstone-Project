package com.debdroid.tinru.ui.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.debdroid.tinru.R;
import com.debdroid.tinru.ui.PointOfInterestListActivity;

import dagger.android.AndroidInjection;
import timber.log.Timber;

public class TinruAppWidgetProvider extends AppWidgetProvider {


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive is called");
        //Always call this before super
        AndroidInjection.inject(this, context);
        super.onReceive(context, intent);
    }

    // This is called to update the App Widget at intervals defined by the updatePeriodMillis
    // attribute in the AppWidgetProviderInfo. This method is also called when the user adds the App Widget.
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("onUpdate is called");
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Set up the intent that starts the TinruListAppWidgetService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, TinruListAppWidgetService.class);

            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.tinru_appwidget_layout);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how the data is populated .
            rv.setRemoteAdapter(R.id.lv_appwidget_list_view, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.lv_appwidget_list_view, R.id.tv_appwidget_empty_view);

            // Set the PointOfInterestListActivity intent to launch when clicked
            Intent appIntent = new Intent(context, PointOfInterestListActivity.class);
            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0,
                    appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.lv_appwidget_list_view, appPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    // This is called when the widget is first placed and any time the widget is resized.
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Timber.d("onAppWidgetOptionsChanged is called");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }


}
