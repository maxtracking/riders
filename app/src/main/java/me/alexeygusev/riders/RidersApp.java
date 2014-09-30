package me.alexeygusev.riders;

/**
 * Created by Alex Gusev on 30/04/2014.
 * Project: Fidel.
 *
 * Copyright (c) 2014. All rights reserved.
 */

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.bugsnag.android.Bugsnag;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class RidersApp extends Application {

    public static final String BUGSNAG_APP_ID = "48a9a965a37eec473b7fea03dbbe3abf";

    private static Context sContext = null;

    @Override
    public void onCreate() {

        RidersApp.sContext = getApplicationContext();

        /**
         * AG: report the crashes always.
         * Comment out "if" line when you don't want to receive debugging reports.
         */
        if (!BuildConfig.DEBUG) {
            Bugsnag.register(this, BUGSNAG_APP_ID);
        }

        /**
         * We do not care about ANR
         */
//        StrictMode.ThreadPolicy.Builder builderThread = new StrictMode.ThreadPolicy.Builder();
//        StrictMode.VmPolicy.Builder builderVM = new StrictMode.VmPolicy.Builder();
//
//        builderThread.permitAll().permitCustomSlowCalls().permitNetwork();

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);//builderThread.build());
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);
//        StrictMode.setVmPolicy(builderVM.detectAll().penaltyLog().build());

        CalligraphyConfig.initDefault("fonts/din1451alt.ttf", R.attr.fontPath);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static Context getAppContext() {
        return RidersApp.sContext;
    }
}
