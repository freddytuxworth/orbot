package org.torproject.android.service.vpn;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VpnPreferences {

    private static final String APP_VPN_PREFERENCES_KEY = "appVpnPreferences";

    private final SharedPreferences preferences;

    private final Context context;

    private final Map<String, AppInformation> apps;

    private final Type appSettingsMapType = new TypeToken<Map<String, AppVpnSettings>>() {}.getType();

    private final Gson gson;

    private final PackageManager packageManager;

    public static class AppVpnSettings {
        public boolean routeThroughTor;

        public static AppVpnSettings defaultSettings(final SharedPreferences preferences) {
            AppVpnSettings appVpnSettings = new AppVpnSettings();
            appVpnSettings.routeThroughTor = preferences.getBoolean("pref_tor_route_default", false);
            return appVpnSettings;
        }
    }

    public static class AppInformation {
        public String name;
        public ApplicationInfo applicationInfo;
        public AppVpnSettings vpnSettings;

        public AppInformation(String name, ApplicationInfo applicationInfo, AppVpnSettings vpnSettings) {
            this.name = name;
            this.applicationInfo = applicationInfo;
            this.vpnSettings = vpnSettings;
        }
    }

    public VpnPreferences(Context context) {
        this.context = context;
        this.preferences = VpnUtils.getSharedPrefs(context);
        this.gson = new Gson();
        this.packageManager = context.getPackageManager();
        this.apps = loadApps();
    }

    private Map<String, AppInformation> loadApps() {
        String settingsJson = preferences.getString(APP_VPN_PREFERENCES_KEY, "[]");
        Map<String, AppVpnSettings> appSettings = gson.fromJson(settingsJson, appSettingsMapType);
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);

        return installedApps.stream()
                .filter(app -> usesInternet(app, packageManager))
                .map(applicationInfo -> {
                    String appName = packageManager.getNameForUid(applicationInfo.uid);
                    AppVpnSettings settings = appSettings.get(appName);
                    if (settings == null)
                        settings = AppVpnSettings.defaultSettings(preferences);
                    return new AppInformation(appName, applicationInfo, settings);
                })
                .collect(Collectors.toMap(appInformation -> appInformation.name, Function.identity()));
    }

    public void load() {
        apps.putAll(loadApps());
    }

    public void saveSettings() {
        Map<String, AppVpnSettings> appSettings = apps.values().stream()
                .collect(Collectors.toMap(
                        app -> app.name,
                        app -> app.vpnSettings
                ));
        String serializedSettings = gson.toJson(appSettings, appSettingsMapType);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(APP_VPN_PREFERENCES_KEY, serializedSettings);
        edit.apply();
    }

    private static boolean usesInternet(ApplicationInfo app, PackageManager packageManager) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo == null || packageInfo.requestedPermissions == null)
                return false;
            return Arrays.asList(packageInfo.requestedPermissions).contains(Manifest.permission.INTERNET);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public Map<String, AppInformation> getAppsByName() {
        return apps;
    }

    public List<AppInformation> getApps() {
        return apps.values().stream()
                .sorted(Comparator.comparing(appInformation -> appInformation.name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public AppInformation getApp(String name) {
        return apps.get(name);
    }

}
