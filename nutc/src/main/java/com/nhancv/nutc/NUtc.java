package com.nhancv.nutc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by nhancao on 4/27/17.
 */

public class NUtc {
    private static final String TAG = NUtc.class.getSimpleName();
    private long cacheLastUtc;
    private long cacheElapsedRealTime;
    private Handler handler;
    private WeakReference<Context> applicationContext;
    private int maxRetryTime; //-1 mean forever
    private int retryTime;
    private boolean enableLog;
    private NCache ncache;

    private NUtc() {
        handler = new Handler();
    }

    public static void build(Context applicationContext) {
        build(applicationContext, null);
    }

    public static void build(Context applicationContext, final UTCTime callback) {
        getInstance().setApplicationContext(applicationContext)
                     .setNcache(new NCache(applicationContext))
                     .setMaxRetryTime(5)
                     .buildUtcTime(callback);
    }

    public static NUtc getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public static void getUTCTimeNtp(final UTCTime callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                long utcTimeStamp = 0;
                NSntpClient sntpClient = new NSntpClient();
                if (sntpClient.requestTime("pool.ntp.org")) {
                    utcTimeStamp = sntpClient.getNtpTime();
                }
                if (utcTimeStamp > 0) {
                    return utcTimeStamp;
                }
                return System.currentTimeMillis();

            }

            @Override
            protected void onPostExecute(Long utcTimeStamp) {
                if (callback != null) {
                    callback.getTime(utcTimeStamp);
                }
            }
        }.execute();
    }

    public static long getUtcNow() {
        long lastUtc = getInstance().getCacheLastUtc();
        if (lastUtc == 0) {
            if (getInstance().isNeedToBuild()) {
                getInstance().log("Need to build again. The below result is local Utc.")
                             .refreshBuildUtcTime();
            }
            long getCacheLastUtcTime = getInstance().getNcache().getCacheLastUtcTime();
            long getCacheElapsedRealTime = getInstance().getNcache().getCacheElapsedRealTime();
            if (getCacheLastUtcTime > 0 && getCacheElapsedRealTime > 0) {
                return getInstance().getUtc(getCacheLastUtcTime, getCacheElapsedRealTime);
            }
            return System.currentTimeMillis();
        }
        return getInstance().getUtc(lastUtc, getInstance().getCacheElapsedRealTime());
    }

    private long getUtc(long lastUtc, long elapsedRealTime) {
        return lastUtc + (SystemClock.elapsedRealtime() - elapsedRealTime);
    }

    public void buildUtcTime(final UTCTime callback) {
        if (isConnected(applicationContext.get())) {
            new AsyncTask<Void, Void, Long>() {
                @Override
                protected Long doInBackground(Void... params) {
                    long utcTimeStamp = 0;
                    NSntpClient sntpClient = new NSntpClient();
                    if (sntpClient.requestTime("pool.ntp.org")) {
                        utcTimeStamp = sntpClient.getNtpTime();
                    }

                    if (utcTimeStamp > 0) {
                        return utcTimeStamp;
                    }
                    return -1L;

                }

                @Override
                protected void onPostExecute(Long utcTimeStamp) {
                    if (utcTimeStamp == -1) {
                        if ((maxRetryTime == -1 || retryTime < maxRetryTime) && isNeedToBuild() &&
                            isConnected(applicationContext.get())) {
                            refreshBuildUtcTime();
                            retryTime++;
                        }
                    } else {
                        retryTime = 0;

                        //cache time
                        cacheLastUtc = utcTimeStamp;
                        cacheElapsedRealTime = SystemClock.elapsedRealtime();
                        ncache.cacheTime(cacheLastUtc, cacheElapsedRealTime);
                        //notify
                        if (callback != null) {
                            callback.getTime(utcTimeStamp);
                        }
                    }
                }
            }.execute();
        }
    }

    public void clearCache() {
        setCacheLastUtc(0);
        setCacheElapsedRealTime(0);
        setMaxRetryTime(5);
        setRetryTime(0);

        if (ncache != null) {
            ncache.clearCache();
        }

    }

    public NCache getNcache() {
        return ncache;
    }

    private NUtc setNcache(NCache ncache) {
        this.ncache = ncache;
        return this;
    }

    public void refreshBuildUtcTime() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                buildUtcTime(new UTCTime() {
                    @Override
                    public void getTime(Long utcTimeStamp) {
                        log("Rebuild success: " + utcTimeStamp);
                    }
                });
            }
        });
    }

    public long getCacheLastUtc() {
        return cacheLastUtc;
    }

    public NUtc setCacheLastUtc(long cacheLastUtc) {
        this.cacheLastUtc = cacheLastUtc;
        return this;
    }

    public long getCacheElapsedRealTime() {
        return cacheElapsedRealTime;
    }

    public NUtc setCacheElapsedRealTime(long cacheElapsedRealTime) {
        this.cacheElapsedRealTime = cacheElapsedRealTime;
        return this;
    }

    private Handler getHandler() {
        return handler;
    }

    public WeakReference<Context> getApplicationContext() {
        return applicationContext;
    }

    private NUtc setApplicationContext(Context applicationContext) {
        this.applicationContext = new WeakReference<>(applicationContext);
        return this;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public NUtc setRetryTime(int retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public int getMaxRetryTime() {
        return maxRetryTime;
    }

    public NUtc setMaxRetryTime(int maxRetryTime) {
        if (maxRetryTime > 0) {
            this.maxRetryTime = maxRetryTime;
        }
        return this;
    }

    public boolean isNeedToBuild() {
        return getCacheLastUtc() <= 0;
    }

    public boolean isConnected(Context appContext) {
        if (appContext != null) {
            ConnectivityManager connectivityManager =
                    ((ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE));
            return connectivityManager.getActiveNetworkInfo() != null &&
                   connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    private boolean isEnableLog() {
        return enableLog;
    }

    public NUtc setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
        return this;
    }

    private NUtc log(String msg) {
        if (isEnableLog()) {
            Log.e(TAG, msg);
        }
        return this;
    }

    public interface UTCTime {
        void getTime(Long utcTimeStamp);
    }

    private static class SingletonHelper {
        private static final NUtc INSTANCE = new NUtc();
    }

    public static class NConnectivity extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (getInstance().isConnected(context)) {
                        if (getInstance().isNeedToBuild()) {
                            getInstance().refreshBuildUtcTime();
                        }
                    }
                    break;
            }
        }

    }
}
