package com.twitch.homescreenlock;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockScreenService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		Log.d("LockScreenService", "Added intent filters for ACTION_SCREEN_ON and ACTION_SCREEN_OFF");
		BroadcastReceiver mReceiver = new LockScreenBroadcast();
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("LockScreenService", "Destroyed");
	}

	public static boolean isRunning() {
		final Context context = HomeScreenLock.getContext();
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(LockScreenService.class.getName())){
                return true;
            }
        }
		return false;
	}
	
	/*@Override
	public void onStart(Intent intent, int startId) {
		/*  boolean screenOn = intent.getBooleanExtra("screen_state", false);
        if (!screenOn) {
            Log.d("Screen off", "screen off"); 
         } else {
       	 Log.d("Screen on", "screen on"); 
         }*/
	//}
}
