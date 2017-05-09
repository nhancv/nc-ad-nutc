package com.nhancv.nutc;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by nhancao on 5/9/17.
 */

class NCache {
    private static final String KEY_CACHED_FILE = "com.nhancv.nutc.KEY_CACHED_FILE";
    private static final String KEY_UTC_TIME = "com.nhancv.nutc.KEY_UTC_TIME";
    private static final String KEY_ELAPSED_TIME = "com.nhancv.nutc.KEY_ELAPSED_TIME";

    private SharedPreferences sharedPreferences = null;

    public NCache(Context context) {
        sharedPreferences = context.getSharedPreferences(KEY_CACHED_FILE, MODE_PRIVATE);
    }

    void clearCache() {
        if (checkNull()) return;
        sharedPreferences.edit().clear().apply();
    }

    void cacheTime(Long utcTimestamp, Long deviceUpTime) {
        if (checkNull()) return;
        sharedPreferences.edit().putLong(KEY_UTC_TIME, utcTimestamp).apply();
        sharedPreferences.edit().putLong(KEY_ELAPSED_TIME, deviceUpTime).apply();
    }

    long getCacheLastUtcTime() {
        if (checkNull()) return -1;
        return sharedPreferences.getLong(KEY_UTC_TIME, 0);
    }

    private boolean checkNull() {
        return sharedPreferences == null;
    }

    long getCacheElapsedRealTime() {
        if (checkNull()) return -1;
        return sharedPreferences.getLong(KEY_ELAPSED_TIME, 0);
    }

}
