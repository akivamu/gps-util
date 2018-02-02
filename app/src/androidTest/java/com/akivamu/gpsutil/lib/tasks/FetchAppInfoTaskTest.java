package com.akivamu.gpsutil.lib.tasks;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.akivamu.gpsutil.lib.data.AppInfo;
import com.akivamu.gpsutil.lib.data.DeviceInfo;
import com.akivamu.gpsutil.lib.data.GoogleIdentity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FetchAppInfoTaskTest {
    private AppInfo appInfo;

    @Test
    public void simpleTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        String packageName = "jp.Appsys.PanecalST";

        GoogleIdentity googleIdentity = new GoogleIdentity();
        googleIdentity.setName("qcautotest01@gmail.com");
        googleIdentity.setToken("twV9mhEAnd__YHwrqCNl4lYEm2eKw07S2TD5obYjYnJM57ss3Pl7SHQRzUnr3ejxK3jK5A.");
        googleIdentity.setGsfId("3567af1117f8fded");

        DeviceInfo deviceInfo = DeviceInfo.buildDefault(appContext);

        final CountDownLatch signal = new CountDownLatch(1);
        FetchAppInfoTask task = new FetchAppInfoTask(appContext, packageName, googleIdentity, deviceInfo, new FetchAppInfoTask.Callback() {
            @Override
            public void onSuccess(AppInfo result) {
                appInfo = result;
                signal.countDown();
            }

            @Override
            public void onError(String error) {
                signal.countDown();
            }
        });
        task.execute();

        signal.await(30, TimeUnit.SECONDS);
        Assert.assertNotNull(appInfo);
        Assert.assertNotNull(appInfo.getDownloadUrl());
        Assert.assertTrue(appInfo.getDownloadUrl().length() > 0);
    }
}
