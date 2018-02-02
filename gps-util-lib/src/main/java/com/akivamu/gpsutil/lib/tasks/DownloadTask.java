package com.akivamu.gpsutil.lib.tasks;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.akivamu.gpsutil.lib.data.ApkFile;
import com.akivamu.gpsutil.lib.data.AppInfo;

import java.io.File;

public class DownloadTask {
    private static final String TAG = DownloadTask.class.getSimpleName();
    // Params
    private final Context context;
    private final AppInfo appInfo;
    private final String downloadFolderInSdcard;
    private final String customApkFileName;
    private final String referrer;
    private final Callback callback;

    private final DownloadManager downloadManager;
    private long enqueuedDownloadId;

    public DownloadTask(Context context, AppInfo appInfo, String downloadFolderInSdcard, String referrer, Callback callback) {
        this(context, appInfo, downloadFolderInSdcard, null, referrer, callback);
    }

    public DownloadTask(Context context, AppInfo appInfo, String downloadFolderInSdcard, String customApkFileName, String referrer, Callback callback) {
        this.context = context;
        this.appInfo = appInfo;
        this.downloadFolderInSdcard = downloadFolderInSdcard;
        this.customApkFileName = customApkFileName;
        this.referrer = referrer;
        this.callback = callback;

        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void execute() {
        String apkFileName = appInfo.getPackageName() + ".apk";
        if (customApkFileName != null) {
            apkFileName = customApkFileName;
        }

        // Build download request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(appInfo.getDownloadUrl()))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(true)
                .setTitle(appInfo.getPackageName())
                .setDescription("Downloading")
                .setDestinationInExternalPublicDir(downloadFolderInSdcard, apkFileName)
                .addRequestHeader("Cookie", "MarketDA=" + appInfo.getMarketDA())
                .addRequestHeader("Referer", referrer); // https://en.wikipedia.org/wiki/HTTP_referer

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        // Enqueue download
        enqueuedDownloadId = downloadManager.enqueue(request);

        // Listen for ACTION_DOWNLOAD_COMPLETE
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == null) {
                    Log.w(TAG, "SKIPPED: Received null action");
                    return;
                }

                // Completed
                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    if (downloadId != enqueuedDownloadId) {
                        Log.w(TAG, "SKIPPED: Received difference download ID: " + downloadId);
                        return;
                    }

                    // Get download status
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);
                    Cursor c = downloadManager.query(q);

                    if (c.moveToFirst()) {
                        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                ApkFile apkFile = new ApkFile(context, new File(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME))));
                                callback.onDownloadCompleted(apkFile);
                                break;
                            case DownloadManager.STATUS_FAILED:
                                callback.onError("Fail to download");
                                break;
                        }
                    }

                    context.unregisterReceiver(this);
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        callback.onDownloadEnqueued(enqueuedDownloadId);
    }

    public interface Callback {
        void onDownloadEnqueued(long downloadId);

        void onDownloadCompleted(ApkFile apkFile);

        void onError(String error);
    }
}