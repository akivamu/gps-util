package com.akivamu.gpsutil.lib;

import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;

import com.akivamu.gpsutil.lib.data.ApkFile;
import com.akivamu.gpsutil.lib.data.AppInfo;
import com.akivamu.gpsutil.lib.data.DeviceInfo;
import com.akivamu.gpsutil.lib.data.GoogleIdentity;
import com.akivamu.gpsutil.lib.tasks.DownloadTask;
import com.akivamu.gpsutil.lib.tasks.FetchAppInfoTask;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

public class ApkDownloader {
    private static final String TAG = ApkDownloader.class.getSimpleName();

    private final Context context;
    private final GoogleIdentity googleIdentity;
    private final DeviceInfo deviceInfo;
    private final String downloadFolderInSdcard;
    @Setter
    private boolean showCompletedNotification;

    private final DownloadManager downloadManager;
    private final Map<String, Long> downloadingPackageName = new HashMap<>();

    public ApkDownloader(Context context, GoogleIdentity googleIdentity, DeviceInfo deviceInfo, String downloadFolderInSdcard) {
        this.context = context;
        this.googleIdentity = googleIdentity;
        this.deviceInfo = deviceInfo;
        this.downloadFolderInSdcard = downloadFolderInSdcard;

        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void requestDownload(final String packageName, final String referrer, final Callback callback) {
        if (downloadingPackageName.get(packageName) != null) {
            Log.w(TAG, "Already requested: " + packageName);
            return;
        }

        // Fetch AppInfo
        FetchAppInfoTask fetchAppInfoTask = new FetchAppInfoTask(context, packageName, googleIdentity, deviceInfo, new FetchAppInfoTask.Callback() {
            @Override
            public void onSuccess(final AppInfo appInfo) {
                if (appInfo.getDownloadUrl() == null || appInfo.getDownloadUrl().length() == 0) {
                    callback.onError("AppInfo: download url null");
                    return;
                }

                // Download
                DownloadTask.Params params = DownloadTask.Params.builder()
                        .appInfo(appInfo)
                        .downloadFolderInSdcard(downloadFolderInSdcard)
                        .referrer(referrer)
                        .showCompletedNotification(showCompletedNotification)
                        .build();
                DownloadTask downloadTask = new DownloadTask(context, params, new DownloadTask.Callback() {
                    @Override
                    public void onDownloadEnqueued(long downloadId) {
                        downloadingPackageName.put(appInfo.getPackageName(), downloadId);
                    }

                    @Override
                    public void onDownloadCompleted(ApkFile apkFile) {
                        downloadingPackageName.remove(appInfo.getPackageName());
                        callback.onSuccess(apkFile);
                    }

                    @Override
                    public void onError(String error) {
                        downloadingPackageName.remove(appInfo.getPackageName());
                        callback.onError(error);
                    }
                });
                downloadTask.execute();
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
        fetchAppInfoTask.execute();
    }

    public void cancelDownload(long... ids) {
        downloadManager.remove(ids);

        for (long id : ids) {
            String packageName = findDownloadingPackageName(id);
            downloadingPackageName.remove(packageName);
        }
    }

    private String findDownloadingPackageName(long id) {
        for (String packageName : downloadingPackageName.keySet()) {
            if (downloadingPackageName.get(packageName) == id) {
                return packageName;
            }
        }

        return null;
    }

    public interface Callback {
        void onSuccess(ApkFile apkFile);

        void onError(String error);
    }
}
