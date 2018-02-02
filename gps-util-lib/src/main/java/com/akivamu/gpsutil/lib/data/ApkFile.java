package com.akivamu.gpsutil.lib.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.io.File;

import lombok.Getter;

public class ApkFile {

    @Getter
    private File file;

    @Getter
    private Bitmap icon;

    public ApkFile(Context context, File file) {
        this.file = file;

        String filePath = file.getPath();
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);

        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;

            if (Build.VERSION.SDK_INT >= 8) {
                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;
            }

            Drawable icon = appInfo.loadIcon(context.getPackageManager());
            this.icon = ((BitmapDrawable) icon).getBitmap();
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ApkFile && ((ApkFile) o).getFile().equals(getFile());
    }
}
