package com.akivamu.gpsutil.lib.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.akivamu.gpsutil.lib.data.AppInfo;
import com.akivamu.gpsutil.lib.data.DeviceInfo;
import com.akivamu.gpsutil.lib.data.GoogleIdentity;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.ApiBuilderException;
import com.github.yeriomin.playstoreapi.AppDetails;
import com.github.yeriomin.playstoreapi.DetailsResponse;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.PlayStoreApiBuilder;

import java.io.IOException;
import java.util.Locale;

import github.yeriomin.NativeDeviceInfoProvider;
import github.yeriomin.NativeHttpClientAdapter;

public class FetchAppInfoTask extends AsyncTask<Void, Void, AppInfo> {
    private static final String TAG = FetchAppInfoTask.class.getSimpleName();

    // Params
    private final Context context;
    private final String packageName;
    private final GoogleIdentity googleIdentity;
    private final DeviceInfo deviceInfo;
    private final Callback callback;

    private String error;

    public FetchAppInfoTask(Context context, String packageName, GoogleIdentity googleIdentity, DeviceInfo deviceInfo, Callback callback) {
        this.context = context;
        this.packageName = packageName;
        this.googleIdentity = googleIdentity;
        this.deviceInfo = deviceInfo;
        this.callback = callback;
    }

    @Override
    protected AppInfo doInBackground(Void... params) {
        GooglePlayAPI api = buildApi();
        if (api != null) {
            return fetchAppInfoSync(packageName, api);
        }
        return null;
    }

    @Override
    protected void onPostExecute(AppInfo appInfo) {
        if (appInfo == null) {
            callback.onError(error);
        } else {
            callback.onSuccess(appInfo);
        }
    }

    private GooglePlayAPI buildApi() {
        NativeDeviceInfoProvider deviceInfoProvider = new NativeDeviceInfoProvider();
        deviceInfoProvider.setContext(context);
        deviceInfoProvider.setLocaleString(Locale.US.toString());

        PlayStoreApiBuilder builder = new PlayStoreApiBuilder()
                .setHttpClient(new NativeHttpClientAdapter())
                .setDeviceInfoProvider(deviceInfoProvider)
                .setLocale(new Locale(deviceInfo.getLocale()))
                .setEmail(googleIdentity.getName())
                .setPassword("123")
                .setGsfId(googleIdentity.getGsfId())
                .setToken(googleIdentity.getToken());
        try {
            return builder.build();
        } catch (ApiBuilderException e) {
            e.printStackTrace();
            error = e.toString();
        } catch (Exception e) {
            e.printStackTrace();
            error = e.toString();
        }

        Log.e(TAG, "Failed to build GooglePlayAPI");
        return null;
    }

    private AppInfo fetchAppInfoSync(String packageName, GooglePlayAPI api) {
        try {
            DetailsResponse response = api.details(packageName);
            DocV2 details = response.getDocV2();
            AppDetails appDetails = details.getDetails().getAppDetails();
            api.purchase(packageName, appDetails.getVersionCode(), details.getOffer(0).getOfferType());
            api.log(packageName, System.currentTimeMillis());
            AndroidAppDeliveryData deliveryData = api.delivery(packageName, appDetails.getVersionCode(), details.getOffer(0).getOfferType())
                    .getAppDeliveryData();

            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setDownloadUrl(deliveryData.getDownloadUrl());
            appInfo.setMarketDA("");

            return appInfo;
        } catch (IOException e) {
            e.printStackTrace();
            error = e.toString();

            Log.e(TAG, "Failed to fetch app info");
            return null;
        }
    }

    public interface Callback {
        void onSuccess(AppInfo appInfo);

        void onError(String error);
    }
}