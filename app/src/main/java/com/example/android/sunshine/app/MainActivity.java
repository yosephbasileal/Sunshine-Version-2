/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Main activity which contains forecast fragment
 */
public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    // Get name of class programatically so that we don't have to hard code, it
    // in case we change it.
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    // Member variables to keep track of settings prefs and UI pane mode.
    private String mLocation;
    private boolean mIsMetric;
    private boolean mTwoPane;

    // To be used to dynamically add a fragment when in two-pane mode.
    private static final String DETAILFRAGMENT_TAG = "FFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Use bundle of state information if present.
        super.onCreate(savedInstanceState);

        // Get setting preferences.
        mLocation = Utility.getPreferredLocation(this);
        mIsMetric = Utility.isMetric(this);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            // One pane mode.
            mTwoPane = false;
            // Add a shadow by elevating the action bar in one-pane mode.
            getSupportActionBar().setElevation(0f);
        }

        // Use "Today" items expanded layout only if in two-pane mode.
        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                         .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);

        // Initialize SyncAdapter with this activity's context.
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Start SettingsActivity if clicked from menu items.
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get location and temperature unit preferences.
        String location = Utility.getPreferredLocation( this );
        boolean isMetric = Utility.isMetric(this);

        // Update the location and units using FragmentManager.
        if (location != null && (!location.equals(mLocation) || isMetric != mIsMetric)) {
            // Update ForecastFragment.
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
                ff.onUnitsChanged();
            }
            // Update DetailFragment if present. (if in two-pane mode)
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(location);
                df.onUnitsChanged(isMetric);
            }

            // Save last used preferences as member variables.
            mLocation = location;
            mIsMetric = isMetric;
        }
    }

    /**
     * A method from a callback interface in ForecastFragment for handling item clicks.
     * */
    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            // In one-pane mode, start DetailActivity and pass URI of item with intent.
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}

