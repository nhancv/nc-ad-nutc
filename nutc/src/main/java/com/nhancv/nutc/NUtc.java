package com.nhancv.nutc;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

/**
 * Created by nhancao on 4/27/17.
 */

public class NUtc {
    private static final String TAG = NUtc.class.getSimpleName();
    private long cacheMemUtc;
    private long deviceUpTime;
    private Handler handler;

    private NUtc() {
        handler = new Handler();
    }

    public static void build(final UTCTime callback) {
        getInstance().buildUTCTime(callback);
    }

    private static NUtc getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public static void getUTCTimeNtp(final UTCTime callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                long utcTimeStamp = 0;
                SntpClient sntpClient = new SntpClient();
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
                callback.getTime(utcTimeStamp);
            }
        }.execute();
    }

    public static long getUtcNow() {
        long lastUtc = getInstance().getCacheMemUtc();
        if (lastUtc == 0) {
            return System.currentTimeMillis();
        }
        return lastUtc + (SystemClock.elapsedRealtime() - getInstance().getDeviceUpTime());
    }

    public void buildUTCTime(final UTCTime callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                long utcTimeStamp = 0;
                SntpClient sntpClient = new SntpClient();
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
                    //@nhancv TODO: try again
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            buildUTCTime(callback);
//                        }
//                    }, 500);
                } else {
                    //cache time
                    cacheMemUtc = Math.max(cacheMemUtc, utcTimeStamp);
                    deviceUpTime = SystemClock.elapsedRealtime();


                    callback.getTime(utcTimeStamp);
                }
            }
        }.execute();
    }

    public long getCacheMemUtc() {
        return cacheMemUtc;
    }

    public void setCacheMemUtc(long cacheMemUtc) {
        this.cacheMemUtc = cacheMemUtc;
    }

    public long getDeviceUpTime() {
        return deviceUpTime;
    }

    public void setDeviceUpTime(long deviceUpTime) {
        this.deviceUpTime = deviceUpTime;
    }

    private Handler getHandler() {
        return handler;
    }

    public interface UTCTime {
        void getTime(Long utcTimeStamp);
    }

    private static class SingletonHelper {
        private static final NUtc INSTANCE = new NUtc();
    }

}
