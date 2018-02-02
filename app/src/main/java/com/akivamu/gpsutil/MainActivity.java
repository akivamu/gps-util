package com.akivamu.gpsutil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.akivamu.gpsutil.lib.ApkDownloader;
import com.akivamu.gpsutil.lib.data.ApkFile;
import com.akivamu.gpsutil.lib.data.DeviceInfo;
import com.akivamu.gpsutil.lib.data.GoogleIdentity;
import com.akivamu.gpsutil.lib.tasks.AccountLoginTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ApkDownloader apkDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginGoogle();
    }

    private void loginGoogle() {
        new AccountLoginTask(this, new AccountLoginTask.Callback() {
            @Override
            public void onSuccess(GoogleIdentity googleIdentity) {
                Toast.makeText(MainActivity.this, "Google: " + googleIdentity.getName(), Toast.LENGTH_SHORT).show();

                apkDownloader = new ApkDownloader(MainActivity.this);
                apkDownloader.setGoogleIdentity(googleIdentity);
                apkDownloader.setDeviceInfo(DeviceInfo.buildDefault(MainActivity.this));
                apkDownloader.setDownloadFolderInSdcard("/");

                apkDownloader.requestDownload("jp.Appsys.PanecalST", "", new ApkDownloader.Callback() {
                    @Override
                    public void onSuccess(ApkFile result) {
                        Log.d(TAG, "Download success: " + result.getFile().getAbsolutePath());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "ERROR: requestDownload: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Google: FAILED", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google login: " + error);
            }
        }).execute();
    }
}
