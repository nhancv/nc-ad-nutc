package com.nhancv.nutc;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by nhancao on 5/9/17.
 */

class Cache {
    private static final String KEY_CACHED_FILE = "com.nhancv.nutc.KEY_CACHED_FILE";
    private static final String KEY_UTC = "com.nhancv.nutc.KEY_UTC";

    private SharedPreferences sharedPreferences = null;

    public Cache(Context context) {
        sharedPreferences = context.getSharedPreferences(KEY_CACHED_FILE, MODE_PRIVATE);
    }

    void clearCache() {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().clear().apply();
    }

    void cacheTime(Long utcTimestamp) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putLong(KEY_UTC, utcTimestamp).apply();
    }

    long getCacheTime() {
        if (sharedPreferences == null) {
            return -1;
        }
        return sharedPreferences.getLong(KEY_UTC, -1);
    }

}
