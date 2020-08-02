/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.torproject.android.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.torproject.android.R;
import org.torproject.android.service.vpn.VpnPreferences;

public class AppManagerActivity extends AppCompatActivity {

    private GridView listApps;
    private ListAdapter adapterApps;
    private ProgressBar progressBar;
    private PackageManager packageManager;
    private VpnPreferences vpnPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.layout_apps);
        setTitle(R.string.apps_mode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        packageManager = getPackageManager();
        listApps = findViewById(R.id.applistview);
        progressBar = findViewById(R.id.progressBar);
        vpnPreferences = new VpnPreferences(getApplicationContext());
        adapterApps = new VpnAppAdapter(getApplicationContext());
        reloadTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadTask.execute();
    }

    /*
     * Create the UI Options Menu (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh_apps)
            reloadTask.execute();
        else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final AsyncTask<Void, Void, Void> reloadTask = new AsyncTask<Void, Void, Void>() {
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        protected Void doInBackground(Void... unused) {
            vpnPreferences.load();
            return null;
        }
        protected void onPostExecute(Void unused) {
            ((BaseAdapter) adapterApps).notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    };

    private class VpnAppAdapter extends ArrayAdapter<VpnPreferences.AppInformation> {

        final LayoutInflater inflater = getLayoutInflater();

        public VpnAppAdapter(Context context) {
            super(context, R.layout.layout_apps_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.layout_apps_item, parent, false);

            final VpnPreferences.AppInformation app = vpnPreferences.getApps().get(position);
            final ImageView iconView = convertView.findViewById(R.id.itemicon);
            iconView.setImageDrawable(packageManager.getApplicationIcon(app.applicationInfo));

            final TextView nameView = convertView.findViewById(R.id.itemtext);
            nameView.setText(packageManager.getApplicationLabel(app.applicationInfo));

            final CheckBox routeThroughTorSwitch = convertView.findViewById(R.id.itemcheck);
            routeThroughTorSwitch.setChecked(app.vpnSettings.routeThroughTor);

            routeThroughTorSwitch.setOnCheckedChangeListener((view, isChecked) -> {
                app.vpnSettings.routeThroughTor = isChecked;
                vpnPreferences.saveSettings();
            });
            return convertView;
        }

    }

}
