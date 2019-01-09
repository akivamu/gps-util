package com.akivamu.gpsutil.lib.tasks;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.akivamu.gpsutil.lib.data.GoogleIdentity;

public class AccountLoginTask extends AsyncTask<Void, Void, GoogleIdentity> {
    private static final String TAG = AccountLoginTask.class.getSimpleName();
    private static final String AUTH_TOKEN_TYPE = "androidmarket";

    private Callback callback;
    private Context context;
    private String error;

    public AccountLoginTask(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected GoogleIdentity doInBackground(Void... voids) {
        AccountManager am = AccountManager.get(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            error = "Don't have permission GET_ACCOUNTS";
            Log.e(TAG, error);
            return null;
        }

        try {
            Account[] accounts = am.getAccountsByType("com.google");
            AccountManagerFuture<Bundle> accountManagerFuture;
            accountManagerFuture = am.getAuthToken(accounts[0], AUTH_TOKEN_TYPE, null, true, null, null);

            Bundle authTokenBundle = accountManagerFuture.getResult();

            GoogleIdentity googleIdentity = new GoogleIdentity();
            googleIdentity.setName(authTokenBundle.getString(AccountManager.KEY_ACCOUNT_NAME));
            googleIdentity.setToken(authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN));
            googleIdentity.setGsfId(getGsfId());

            Log.d(TAG, "GoogleIdentity - name: " + googleIdentity.getName());
            Log.d(TAG, "GoogleIdentity - token: " + googleIdentity.getToken());
            Log.d(TAG, "GoogleIdentity - GsfId: " + googleIdentity.getGsfId());

            return googleIdentity;

        } catch (Exception e) {
            e.printStackTrace();
            error = e.toString();
            return null;
        }
    }

    @Override
    protected void onPostExecute(GoogleIdentity googleIdentity) {
        if (googleIdentity == null || googleIdentity.getToken() == null) {
            callback.onError(error);
        } else {
            callback.onSuccess(googleIdentity);
        }
    }

    private String getGsfId() {
        Cursor c = context.getContentResolver().query(
                Uri.parse("content://com.google.android.gsf.gservices"),
                null,
                null,
                new String[]{"android_id"},
                null
        );

        if (c == null || !c.moveToFirst() || c.getColumnCount() < 2) {
            Log.e(TAG, "getGsfId() not data found in gservices");
            return null;
        }

        try {
            return Long.toHexString(Long.parseLong(c.getString(1)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            c.close();
        }
    }

    public interface Callback {
        void onSuccess(GoogleIdentity googleIdentity);

        void onError(String error);
    }
}
