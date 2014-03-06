package com.twitch.homescreenlock;

import java.util.HashMap;

import com.twitch.homescreenlock.GeocodingTask.Param;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class TwitchCensusFlow extends Activity implements View.OnClickListener, LocationListener {

	// State to handle asynchronous location updating
	LocationManager locationManager; 
	String towers;
	
	// System.currentTimeMillis() when Twitch focused on Android screen.
	// Set in onWindowFocusChange
	static long startTime;
	
	/**
	 * ================================================================
	 * onCreate: Load your view, set up state variables/listeners, etc.
	 * ================================================================
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Select a view from res/layout to display
		setContentView(R.layout.twitch_census_flow);
		
		// [Utility - Don't remove] Set Twitch to appear as the home screen.
		setupHomeScreen();
		
		// [Utility - Don't remove] Set up asynchronous location updates.
		setupLocationListening();
		
		/**
		 * TODO: initialize/register for updates/set listeners for any interactive
		 * elements in your XML layout. Example follows.
		 * 
		 * QUESTION: is this necessary for non-interactive elements? It doesn't seem like any
		 * others are needed.
		 */
		
		// Example: retrieving an object specified in the XML layout
		Button doneButton = (Button) findViewById(R.id.flow_button);
		
		
		// Example interactivity: just set this class as button's listener
		doneButton.setOnClickListener(this);
	}
	
	
	/**
	 * =============================================================
	 * User input handling: implement listeners for your interactive
	 * elements. TODO: custom Census/Flow code. Example is given
	 * as a simple button that finishes the activity.
	 * =============================================================
	 */
	
	@Override
	public void onClick(View v) {
		// Get user's load and submit times. There are two possible start times:
		// when the screen turns on or when Twitch comes into focus. We only
		// want the later start time to accurately measure duration.
		long endTime = System.currentTimeMillis();
		long laterStartTime = Math.max(startTime, LockScreenBroadcast.startTime);
		
		// Retrieve updated user location.
		Float latitude = TwitchUtils.getCurrLatitude(this);
		Float longitude = TwitchUtils.getCurrLongitude(this);
		
		/**
		 * TODO: capture the user input we want and update CensusRepsonse.java
		 * to cover Census/Flow. Eventually we will send it to the server
		 * in a CensusPostTask or store it locally on the phone using
		 * a TwitchDatabase.java interface.
		 * 
		 * For now, an example of logging (in the LogCat tab in Eclipse) is shown.
		 * The first argument is a searchable tag for the message in the second argument.
		 */
		 try{
			Log.d("CensusFlow", "Response at (" + latitude + ", " + longitude +
				") started at " + laterStartTime + ", ended at " + endTime);
			// Send POST to Twitch server - from Dress
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(latitude));
				params.put("longitude", Float.toString(longitude));
				params.put("clientStartTime", Long.toString(laterStartTime));
				params.put("clientEndTime", Long.toString(endTime));
				//CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.FLOW, this);

				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				//CensusPostTask.Param p = cpt.new Param(TwitchCensusFlow.this, cr);
				//cpt.execute(p);
			}
			else { // If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(TwitchCensusFlow.this);
				entry.open();
				//not sure what the entry method is for flow
				//entry.createEntryDressCensus(typeOfDress, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime); 
				entry.close(); 
			}
		 }
		 catch(Exception e){
		 	e.printStackTrace();
		 }
		
		// TODO: Display Toast message (short popup) to user?
		// Possibly discretize by direction
		
		// [Utility - Put in exit condition] Remove Twitch as home screen, poll server
		// for aggregates, and finish this Activity. Call this last.
		finishCensusFlow();
	}

	
	/**
	 * ================================
	 * Helpers to simplify starter code
	 * Shouldn't need to modify
	 * ================================
	 */
	
	private void finishCensusFlow() {
		PackageManager pm = getPackageManager(); //Disables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

		TwitchDatabase aggregate = new TwitchDatabase(TwitchCensusFlow.this);
		aggregate.open();
		aggregate.checkAggregates(this);
		finish();
	}
	
	private void setupLocationListening() {
		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
	}
	
	private void setupHomeScreen() {
		// Set activity to start on home screen turn on
		// Don't modify this code
		PackageManager pm = getPackageManager();
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		TwitchUtils.disableUserLockScreen(this);
	}
	
	
	/**
	 * ======================================================
	 * Interface/Android UI compliance and location updating.
	 * You shouldn't need to change these.
	 * ======================================================
	 */
	
	@Override
	protected void onPause() {		
		super.onPause();
		if(towers!=null && locationManager!=null)
			locationManager.removeUpdates(this);
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		if(towers!=null && locationManager!=null) {
			// Register for the updates when Activity is in foreground
			locationManager.requestLocationUpdates(towers, 20000, 1, this);
		}
		
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
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if(hasFocus)
	    {
	    	Log.d("WindowFocus", "Focus on"); 
	    	startTime = System.currentTimeMillis();	
	    }	    
	}
	
	@Override
	public void onLocationChanged(Location l) {
		TwitchUtils.setCurrLocation(this, (float) l.getLatitude(), (float) l.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// Auto-generated method stub
	}
}
