package org.torproject.android.mini.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.torproject.android.mini.R;
import org.torproject.android.service.vpn.VpnPreferences;

public class AppConfigActivity extends AppCompatActivity {

    VpnPreferences.AppInformation mApp;

    private boolean mAppTor = false;
    private boolean mAppData = false;
    private boolean mAppWifi = false;

    private VpnPreferences vpnPreferences;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vpnPreferences = new VpnPreferences(getApplicationContext());
        packageManager = getPackageManager();

        final String appName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);


        mApp = vpnPreferences.getApp(appName);
        getSupportActionBar().setIcon(packageManager.getApplicationIcon(mApp.applicationInfo));
        setTitle(mApp.name);

        Switch switchAppTor = findViewById(R.id.switch_app_tor);
        switchAppTor.setChecked(mAppTor);
        switchAppTor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mApp.vpnSettings.routeThroughTor = isChecked;
                vpnPreferences.saveSettings();

                Intent response = new Intent();
                setResult(RESULT_OK,response);
            }
        });

        Switch switchAppData = findViewById(R.id.switch_app_data);
        switchAppData.setEnabled(false);

        Switch switchAppWifi = findViewById(R.id.switch_app_wifi);
        switchAppWifi.setEnabled(false);



    }

    /*
     * Create the UI Options Menu (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.menu_remove_app) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }




}
