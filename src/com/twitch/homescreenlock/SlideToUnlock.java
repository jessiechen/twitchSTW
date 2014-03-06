package com.twitch.homescreenlock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SlideToUnlock extends Activity implements LocationListener, OnSeekBarChangeListener{

	int finalProgress = 0;
	double latitude = 0.0;
	double longitude = 0.0;
	LocationManager locationManager; 
	String towers; 
	Geocoder geocoder;
	List<Address> addresses;
	static boolean activeUnlock = false;

	String timeStamp = null; 
	String location = null;	
	String latlong = null; 
	boolean success = true; 
	String address = null; String city = null; String country = null;
	static long startTime;
	long endTime;
	String toastText = null;

	@Override
	public void onBackPressed() {
		//Handles no action upon back button press
	}
	
	@Override
	public void onUserLeaveHint() {
		Log.d("TwitchHome", "Detected home button press");
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)
			return true;
		else
			return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.slide_to_unlock);

		SeekBar seekEnergy = (SeekBar) findViewById(R.id.seekBarUnlock);
		ImageView iv = (ImageView)findViewById(R.id.imageViewLocks); //Default		

		PackageManager pm = getPackageManager(); //Enables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);

		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		int unlockLevelMax = 1000; 
		int unlockLevel = 50; //Default. 
		seekEnergy.setMax(unlockLevelMax);
		seekEnergy.setProgress(unlockLevel); 
		seekEnergy.setOnSeekBarChangeListener(this);

		iv.setImageResource(R.drawable.closedlock); //Default

		if(TwitchUtils.isOnline(this)) //ON INTERNET  
		{
			Log.d("Internet", "ON");
		}
		else //NO INTERNET 
		{
			Log.d("Internet", "OFF"); 
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if(hasFocus)
	    {
	    	Log.d("WindowFocus", "Focus on"); 
	    	startTime = System.currentTimeMillis();	
	    }	    
	}


	@Override
	public void onStart() {
		super.onStart();
		activeUnlock = true;
	} 

	@Override
	public void onStop() {
		super.onStop();
		activeUnlock = false;
	}

	@Override
	protected void onPause() {		
		super.onPause();
		if(towers!=null && locationManager!=null)
			locationManager.removeUpdates(this);
	}
	//Register for the updates when Activity is in foreground
	@Override
	protected void onResume() {		
		super.onResume();
		if(towers!=null && locationManager!=null)
			locationManager.requestLocationUpdates(towers, 20000, 1, this);
	}

	//Menu stuff begins.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home_screen_lock, menu); 
		return true;
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {		
		Intent intentSettings = new Intent(this, LockScreenSettings.class);  

		switch(item.getItemId()){		
		case R.id.settings:			
			startActivity(intentSettings); //starts the activity 
			return true; 
		case R.id.exit:
			PackageManager pm = getPackageManager(); //Disables the activity to be home.
			ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
			pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

			TwitchUtils.registerExit(this);
			finish(); 
			return true;		
		default: 
			return false; 
		}

	}
	//Menu stuff ends.

	@Override
	public void onLocationChanged(Location l) {
		// Auto-generated method stub
		latitude = (double) l.getLatitude(); 
		longitude = (double) l.getLongitude(); 

	}
	@Override
	public void onProviderDisabled(String provider) {
		// Auto-generated method stub
	}
	@Override
	public void onProviderEnabled(String provider) {
		// Auto-generated method stub
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Auto-generated method stub
	}
	@Override
	public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
		finalProgress = progress;
		ImageView iv = (ImageView)findViewById(R.id.imageViewLocks);
		final int[] images = {R.drawable.closedlock, R.drawable.openlock}; 

		if(progress>=0 && progress <500)
		{
			Log.d("SEEK","progress=" + progress + ", state=LOCKED");
			iv.setImageResource(images[0]);
		}
		if(progress>=500 && progress <= 1000)	
		{
			Log.d("SEEK","progress=" + progress + ", state=UNLOCKED");
			iv.setImageResource(images[1]);
		}		

	}
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// Auto-generated method stub
	}
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		endTime = System.currentTimeMillis();		

		try
		{
			if(TwitchUtils.isOnline(this)) //Prevents from crashing if no internet. 
			{
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
				Criteria criteria = new Criteria(); 
				towers = locationManager.getBestProvider(criteria, false); 
				Location locationFromGPS = locationManager.getLastKnownLocation(towers);		

				if(locationFromGPS != null)
				{
					latitude = (double) (locationFromGPS.getLatitude()); 
					longitude = (double) (locationFromGPS.getLongitude()); 
					Log.d("LatLong", "Lat= " + latitude + " Long= " + longitude); 
					// We don't need to display anything afterwards, so text location is unnecessary.			
				}
				else
				{
					Log.d("GPS", "Not found"); 
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
		}


		try{			 
			timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			location = address + " " + city + " " + country ;
			latlong = String.valueOf(latitude) + ", " + String.valueOf(longitude);

			//Calculating the duration of time spent on the lock screen. 
			long durationInMiliSecondsViaFocus = endTime - startTime; 
			long durationInMiliSecondsViaScreenOn = endTime - LockScreenBroadcast.startTime;
			Log.d("DurationForSlideToUnlock", "Duration in milisecs via Focus= " + durationInMiliSecondsViaFocus); 
			Log.d("DurationForSlideToUnlock", "Duration in milisecs via ScreenOn= " + durationInMiliSecondsViaScreenOn);
			long durationInMiliSeconds = 0; 
			if(durationInMiliSecondsViaFocus < durationInMiliSecondsViaScreenOn)
			{
				durationInMiliSeconds = durationInMiliSecondsViaFocus; 
			}
			else if(durationInMiliSecondsViaScreenOn < durationInMiliSecondsViaFocus)
			{
				durationInMiliSeconds = durationInMiliSecondsViaScreenOn; 
			}
			Log.d("DurationForSlideToUnlock", "Duration in milisecs= " + durationInMiliSeconds); 
			long laterStartTime = startTime > LockScreenBroadcast.startTime ? startTime : LockScreenBroadcast.startTime;
			
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Double.toString(latitude));
				params.put("longitude", Double.toString(longitude));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("progress", Integer.toString(finalProgress));
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.SLIDE_TO_UNLOCK, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(SlideToUnlock.this, cr);
				cpt.execute(p);
			}
			else {
				// If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(SlideToUnlock.this);
				entry.open();
				entry.createSlideToUnlock(finalProgress, latitude, longitude, laterStartTime, endTime); 
				entry.close(); 
			}
		}
		catch(Exception e){
			success = false; 
			e.printStackTrace(); 
		}
		finally{
			if(success){
				Log.d("DATABASE ENTRY UNLOCK", "SUCCESS"); 
			}				
		}

		PackageManager pm = getPackageManager(); //Disables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

		
		TwitchDatabase aggregate = new TwitchDatabase(SlideToUnlock.this);
		aggregate.open();
		aggregate.checkAggregates(this);
		finish();
	}

}
