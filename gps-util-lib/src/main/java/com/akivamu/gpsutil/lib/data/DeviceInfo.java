package com.akivamu.gpsutil.lib.data;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Locale;
import java.util.Random;

import lombok.Data;

@Data
public class DeviceInfo {
    private String sdkVersion;
    private String device;
    private String sdkInt;
    private String operatorName;
    private String operatorNumeric;
    private String locale;
    private String country;

    public static DeviceInfo buildDefault(Context context) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.sdkVersion = generateRandomSdkVersion();
        deviceInfo.device = Build.DEVICE;
        deviceInfo.sdkInt = String.valueOf(Build.VERSION.SDK_INT);

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            deviceInfo.operatorName = tm.getNetworkOperatorName();
            deviceInfo.operatorNumeric = tm.getNetworkOperator();
        }

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        deviceInfo.locale = locale.getLanguage().toLowerCase();
        deviceInfo.country = locale.getCountry().toLowerCase();

        return deviceInfo;
    }

    private static String generateRandomSdkVersion() {
        Random random = new Random();
        return String.valueOf(random.nextInt(9999999));
    }

}
