package com.runnirr.aaotdfetcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Adam on 6/2/13.
 *
 * Service for running AATOD in the background
 */
public class AAOTDService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handleCommand(intent);


        return START_NOT_STICKY;
    }
}
