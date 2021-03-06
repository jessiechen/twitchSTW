package com.twitch.homescreenlock;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.twitch.homescreenlock.GeocodingTask.Param;

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
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitchCensusActivity extends Activity implements View.OnClickListener, LocationListener {

	LocationManager locationManager; 
	String towers;
	static boolean activeDress = false;
	static long startTime;
	long endTime; 

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
		setContentView(R.layout.twitch_census_activity);

		PackageManager pm = getPackageManager(); //Enables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		
		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		ImageButton imageButtonHome = (ImageButton) findViewById(R.id.imageButtonHome);
		ImageButton imageButtonWork = (ImageButton) findViewById(R.id.imageButtonWork);
		ImageButton imageButtonSocial = (ImageButton) findViewById(R.id.imageButtonSocial);		
		ImageButton imageButtonExcercise = (ImageButton) findViewById(R.id.imageButtonExcercise);
		ImageButton imageButtonFood = (ImageButton) findViewById(R.id.imageButtonFood);
		ImageButton imageButtonTransit = (ImageButton) findViewById(R.id.imageButtonTransit);
		imageButtonHome.setOnClickListener(this); 
		imageButtonWork.setOnClickListener(this); 
		imageButtonSocial.setOnClickListener(this); 		
		imageButtonExcercise.setOnClickListener(this);
		imageButtonFood.setOnClickListener(this); 	
		imageButtonTransit.setOnClickListener(this); 	

		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
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


	public void onClick(View view) {
		
		endTime = System.currentTimeMillis();
		String typeOfActivity = null;
		boolean success = true; 
		String toastText = null;

		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();

		View layout = inflater.inflate(R.layout.twitch_census_toast,
		  (ViewGroup) findViewById(R.id.custom_toast_layout_id));

		// Possibility to set an image
		ImageView image = (ImageView) layout.findViewById(R.id.imageToast);
		
		// Set image for toast while selecting value of response
		switch(view.getId()){
		case R.id.imageButtonHome:
			Log.d("CLICK","imageButtonHome");			
			typeOfActivity = "home";		
			image.setImageResource(R.drawable.home);
			break;
		case R.id.imageButtonWork:
			Log.d("CLICK","imageButtonWork");			
			typeOfActivity = "work";
			image.setImageResource(R.drawable.work);
			break; 
		case R.id.imageButtonSocial:
			Log.d("CLICK","imageButtonSocial");			
			typeOfActivity = "social";		
			image.setImageResource(R.drawable.social);
			break; 	

		case R.id.imageButtonExcercise:
			Log.d("CLICK","imageButtonExcercise");
			typeOfActivity = "exercise";
			image.setImageResource(R.drawable.exercise);
			break; 
		case R.id.imageButtonFood:
			Log.d("CLICK","imageButtonFood");
			typeOfActivity = "food";
			image.setImageResource(R.drawable.eating);
			break;
		case R.id.imageButtonTransit:
			Log.d("CLICK","imageButtonTransit");
			typeOfActivity = "transit";
			image.setImageResource(R.drawable.transit);
			break;

		}

		try{
			//Calculating the duration of time spent on the lock screen. 
			long durationInMiliSecondsViaFocus = endTime - startTime; 
			long durationInMiliSecondsViaScreenOn = endTime - LockScreenBroadcast.startTime;
			Log.d("DurationForActivity", "Duration in milisecs via Focus= " + durationInMiliSecondsViaFocus); 
			Log.d("DurationForActivity", "Duration in milisecs via ScreenOn= " + durationInMiliSecondsViaScreenOn);
			long durationInMiliSeconds = 0;
			if(durationInMiliSecondsViaFocus < durationInMiliSecondsViaScreenOn)
			{
				durationInMiliSeconds = durationInMiliSecondsViaFocus; 
			}
			else if(durationInMiliSecondsViaScreenOn < durationInMiliSecondsViaFocus)
			{
				durationInMiliSeconds = durationInMiliSecondsViaScreenOn;
			}
			Log.d("DurationForActivity", "Duration in milisecs= " + durationInMiliSeconds); 
			
			long laterStartTime = startTime > LockScreenBroadcast.startTime ? startTime : LockScreenBroadcast.startTime;
			// Send POST to Twitch server
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(TwitchUtils.getCurrLatitude(this)));
				params.put("longitude", Float.toString(TwitchUtils.getCurrLongitude(this)));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("activity", typeOfActivity);
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.ACTIVITY, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(TwitchCensusActivity.this, cr);
				cpt.execute(p);
			}
			else { // If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(TwitchCensusActivity.this);
				entry.open();
				entry.createEntryActivityCensus(typeOfActivity, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime); 
				entry.close();
			}
		}
		catch(Exception e){
			success = false;
			e.printStackTrace();
		}
		finally{
			if(success){
				Log.d("DATABASE ENTRY DRESS CENSUS", "SUCCESS"); 
			}				
		}

		PackageManager pm = getPackageManager(); //Disables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
		
		// Set a message
		TwitchDatabase aggregate = new TwitchDatabase(TwitchCensusActivity.this);
		aggregate.open();
		int nextToastPercentage = aggregate.getPercentageForResponse(TwitchConstants.AGGREGATE_TYPE_NAMES[1], typeOfActivity);
		int nextToastPercentageOf = aggregate.getNumResponses(TwitchConstants.AGGREGATE_TYPE_NAMES[1], typeOfActivity);
		
		toastText = String.valueOf(nextToastPercentage);
		TextView text = (TextView) layout.findViewById(R.id.textToast);		
		text.setText(toastText + "% ");
		TextView textPercentOf = (TextView) layout.findViewById(R.id.textToastPercentOf);
		String respStr = nextToastPercentageOf == 1 ? " response " : " responses ";
		textPercentOf.setText(" of " + nextToastPercentageOf + respStr);
		
		TextView textLocation = (TextView) layout.findViewById(R.id.textToastLocation);
		if(TwitchUtils.getGeocodingStatus(this) && TwitchUtils.getGeocodingSuccess(this)) {
			// GeocodingStatus is true if the asynchronous task finished
			// GeocodingSuccess was true if we were able to get a valid address
			textLocation.setText("near " + TwitchUtils.getLocationText(this));
		} else if(TwitchUtils.canUsePrevLocation(this)) {
			// If current location
			textLocation.setText("near " + TwitchUtils.getLocationText(this));
		} else {
			textLocation.setText("near you");
		}

		// Display toast
		final Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toast.cancel();
			}
		}, TwitchConstants.TOAST_DURATION);
		
		// TODO move this to CensusPostTask
		aggregate.checkAggregates(this);
		finish();
	}

	@Override
	public void onStart() {
		super.onStart();
		activeDress = true;
	} 

	@Override
	public void onStop() {
		super.onStop();
		activeDress = false;
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
		
		// Fire off a geocoding task whenever the screen is turned on or
		// the phone returns to Twitch from a higher priority app.
		TwitchUtils.setGeocodingStatus(this, false);
		GeocodingTask task = new GeocodingTask();
		GeocodingTask.Param param = task.new Param(this, locationManager, towers);
		task.execute(param);
		Log.d("Geocoding", "Launched asynchronous geocoding task");
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
		TwitchUtils.setCurrLocation(this, (float) l.getLatitude(), (float) l.getLongitude()); 
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
}
