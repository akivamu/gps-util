package com.akivamu.gpsutil.lib;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.akivamu.gpsutil.lib.data.ApkFile;
import com.akivamu.gpsutil.lib.data.DeviceInfo;
import com.akivamu.gpsutil.lib.data.GoogleIdentity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ApkDownloaderTest {
    private ApkFile resultApkFile;
    @Test
    public void failedWhenNotSetup() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        ApkDownloader apkDownloader = new ApkDownloader(appContext);

        String packageName = "jp.Appsys.PanecalST";

        Assert.assertFalse(apkDownloader.requestDownload(packageName));

        // Partial setup
        apkDownloader.setDownloadFolderInSdcard("/");
        Assert.assertFalse(apkDownloader.requestDownload(packageName));

        apkDownloader.setGoogleIdentity(new GoogleIdentity());
        Assert.assertFalse(apkDownloader.requestDownload(packageName));
    }

    @Test
    public void success() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        ApkDownloader apkDownloader = new ApkDownloader(appContext);

        apkDownloader.setDownloadFolderInSdcard("/");

        GoogleIdentity googleIdentity = new GoogleIdentity();
        googleIdentity.setName("qcautotest01@gmail.com");
        googleIdentity.setToken("twV9mhEAnd__YHwrqCNl4lYEm2eKw07S2TD5obYjYnJM57ss3Pl7SHQRzUnr3ejxK3jK5A.");
        googleIdentity.setGsfId("3567af1117f8fded");
        apkDownloader.setGoogleIdentity(googleIdentity);

        DeviceInfo deviceInfo = DeviceInfo.buildDefault(appContext);
        apkDownloader.setDeviceInfo(deviceInfo);

        String packageName = "jp.Appsys.PanecalST";

        final CountDownLatch signal = new CountDownLatch(1);
        boolean enqueuedDownload = apkDownloader.requestDownload(packageName, new ApkDownloader.Callback() {
            @Override
            public void onSuccess(ApkFile apkFile) {
                resultApkFile = apkFile;
                signal.countDown();
            }

            @Override
            public void onError(String error) {
                signal.countDown();
            }
        });
        Assert.assertTrue(enqueuedDownload);
        signal.await(60, TimeUnit.SECONDS);
        Assert.assertNotNull(resultApkFile);
        Assert.assertTrue(resultApkFile.getFile().exists());
    }
}
