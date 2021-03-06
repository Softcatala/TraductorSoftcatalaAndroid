/*
 * Copyright (C) 2016 Jordi Mas i Hernàndez <jmas@softcatala.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.softcatala.traductor;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

public class Analytics {

    private final String TRACKER_CODE = "UA-85840700-1";
    private final String TRACKER_CODE_DEBUG = "UA-86221143-1";
    private static Tracker _tracker;
    Activity _activity;

    public Analytics(Activity activity) {
        _activity = activity;
        _tracker = getTracker();
    }

    synchronized private Tracker getTracker() {
        if (_tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(_activity);
            _tracker = analytics.newTracker(BuildConfig.DEBUG ? TRACKER_CODE_DEBUG : TRACKER_CODE);
            SetUpUncaughtExceptionHandler();
        }
        return _tracker;
    }

    public void SendEvent(String action, String label) {

        Tracker tracker = getTracker();
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Categoria")
                .setLabel(label)
                .setAction(action)
                .build());

        Log.d("softcatala", "SendEvent for action: " + action);
    }

    public void SendException(String description) {

        Tracker tracker = getTracker();
        tracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(description)
                .build());

        Log.d("softcatala", "SendEvent for exception: " + description);
    }

    private void SetUpUncaughtExceptionHandler() {

        ExceptionReporter handler =
                new ExceptionReporter(getTracker(),
                        Thread.getDefaultUncaughtExceptionHandler(),
                        _activity);

        StandardExceptionParser exceptionParser =
                new StandardExceptionParser(_activity, null) {
                    @Override
                    public String getDescription(String threadName, Throwable t) {
                        String description = "{" + threadName + "} " + Log.getStackTraceString(t);
                        return description;
                    }
                };

        handler.setExceptionParser(exceptionParser);
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

}
