/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.torproject.android.mini.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import org.torproject.android.mini.R;

public class AppManagerActivity extends AppCompatActivity {

    private GridView listApps;
    private ListAdapter adapterApps;
    private ProgressBar progressBar;
    PackageManager pMgr = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pMgr = getPackageManager();

        this.setContentView(R.layout.layout_apps);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listApps = findViewById(R.id.applistview);
        progressBar = findViewById(R.id.progressBar);
    }



}
