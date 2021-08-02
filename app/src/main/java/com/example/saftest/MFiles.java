package com.example.saftest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class MFiles {
    private static Uri mFolderUri;
    private static final String prefKey = "savePathUri";

    public static Uri getmFolderUri() {
        return mFolderUri;
    }

    public static void setmFolderUri(Uri uri) {
        mFolderUri = uri;
    }

    public static Uri loadMFolderUri(Context context) {
        if (getmFolderUri() == null) {
            SharedPreferences sharedPref = getSettings(context);
            String sUri = sharedPref.getString(prefKey, null);
            if (sUri != null && sUri.length() > 0) {
                setmFolderUri(Uri.parse(sUri));
            }
        }
        return getmFolderUri();
    }

    public static void saveMFolderUri(Context context, Uri uri) {
        setmFolderUri(uri);

        SharedPreferences sharedPref = getSettings(context);
        if (uri != null) {
            sharedPref.edit()
                    .putString(prefKey, uri.toString())
                    .apply();
        } else if (sharedPref.contains(prefKey)) {
            sharedPref.edit()
                    .remove(prefKey)
                    .apply();
        }
    }

    private static SharedPreferences getSettings(Context context) {
        String packagePrefs = context.getPackageName() + "_preferences";
        return context.getSharedPreferences(packagePrefs, Context.MODE_PRIVATE);
    }
}
