package com.akivamu.gpsutil.lib.tasks;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.akivamu.gpsutil.lib.data.ApkFile;
import com.akivamu.gpsutil.lib.data.AppInfo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DownloadTaskTest {
    private long enqueuedDownloadId;
    private ApkFile resultApkFile;

    @Test
    public void simpleTest() throws Exception {
        final Context appContext = InstrumentationRegistry.getTargetContext();

        AppInfo appInfo = new AppInfo();
        appInfo.setDownloadUrl("https://play.googleapis.com/download/by-token/download?token=AOTCm0QtpNxYqFjTVMzK1IsdSpls4UK4GFZfTH3CMGmR-h8rif3etTRezGI9qVNKAjX8TsD_7_UYRrhpPdUqixLyMmeChV1XA_I2LBgQ04UMseTKF3KxCf3TVK2T8GdylfwQDKzXhxvnaBDoyqz7p79W4dJTGAAc_rlndrkD-fN8JsrDB8I6zhWOgkj6o0Puk8MUYIdTBSEnsjmQRkOZ04mcQLLLVD22ntdqTDOtGszwMzMjTYMXx5Ei6U-OnEVeXQjiVNojn1HIHXVnhmPyXuKdvqxEcgaYRBHYC_53Ty7T67u1AcwMw5Ebl_d89GviuOagYlUKA9D-wLu4tUIOaNQ&cpn=pnLUUrBE7O7abSOH");
        appInfo.setMarketDA("");
        appInfo.setPackageName("jp.Appsys.PanecalST");

        String savedApkFileName = System.currentTimeMillis() + ".apk";

        final CountDownLatch signal = new CountDownLatch(1);
        DownloadTask.Params params = DownloadTask.Params.builder()
                .appInfo(appInfo)
                .downloadFolderInSdcard("/")
                .customApkFileName(savedApkFileName)
                .referrer("")
                .showCompletedNotification(false)
                .build();
        DownloadTask downloadTask = new DownloadTask(appContext, params, new DownloadTask.Callback() {
            @Override
            public void onDownloadEnqueued(long downloadId) {
                enqueuedDownloadId = downloadId;
            }

            @Override
            public void onDownloadCompleted(ApkFile apkFile) {
                resultApkFile = apkFile;
                signal.countDown();
            }

            @Override
            public void onError(String error) {
                signal.countDown();
            }
        });
        downloadTask.execute();

        signal.await(60, TimeUnit.SECONDS);
        Assert.assertTrue(enqueuedDownloadId > 0);
        Assert.assertNotNull(resultApkFile);
        Assert.assertTrue(resultApkFile.getFile().exists());

        resultApkFile.getFile().delete();
    }
}
