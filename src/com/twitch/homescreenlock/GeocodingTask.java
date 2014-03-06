package com.twitch.homescreenlock;

import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

public class GeocodingTask extends AsyncTask<GeocodingTask.Param, Void, Void> {
	
	class Param {
		LocationManager locationManager; 
		String towers;
		Activity a;
		public Param(Activity a, LocationManager locationManager, String towers) {
			this.a = a;
			this.locationManager = locationManager;
			this.towers = towers;
		}
	}
	
	// The geocoding contract: try to finish geocoding (which involves
	// a networking component) before the user presses a button. On success,
	// PREF_GEOCODING_DONE is set to true (to avoid inaccuracies, this is the
	// last thing done) and a location will be available in PREF_LOCATION_TEXT.
	// If not, we will try to use the value in PREF_LOCATION_TEXT if the latlong
	// hash not changed much from the last update. The last resort is "near you".
	
	@Override
	protected Void doInBackground(Param... args) {
		Param p = args[0];
		boolean success = false;
		try {
			Location locationFromGPS = p.locationManager.getLastKnownLocation(p.towers);		
			double newLatitude = 0.0;
			double newLongitude = 0.0;
			boolean gotLocation = false;
			if(locationFromGPS != null) {
				gotLocation = true;
				newLatitude = locationFromGPS.getLatitude(); 
				newLongitude = locationFromGPS.getLongitude();
				TwitchUtils.setCurrLocation(p.a, (float) newLatitude, (float) newLongitude);
				Log.d("Geocoding", "Pulled loc: Lat= " + newLatitude + " Long= " + newLongitude); 
			} else {
				Log.d("Geocoding", "Couldn't get location using cell towers");
			}
			
			if(gotLocation && TwitchUtils.isOnline(p.a) && Geocoder.isPresent())
			{ //Prevents geocoding from crashing if no internet.
				Geocoder geocoder = new Geocoder(p.a, Locale.getDefault());
				List<Address> addresses = geocoder.getFromLocation(TwitchUtils.getCurrLatitude(p.a), TwitchUtils.getCurrLongitude(p.a), 1);
				if(addresses != null && addresses.size() > 0) {
					Address address = addresses.get(0);
					String locality = address.getLocality();
					String state = address.getAdminArea();
					if(locality != null) {
						String location = locality;
						if(state != null) location += ", " + TwitchUtils.getStateCode(state);
						TwitchUtils.setLocationText(p.a, location);
						TwitchUtils.setPrevLocationStatus(p.a, true);
						TwitchUtils.setPrevLocation(p.a, (float) newLatitude, (float) newLongitude);
						Log.d("Geocoding", "Success. Setting location [" + location + "] for latlong (" + newLatitude + ", " + newLongitude + ")");
						success = true;
					}
					Log.d("Geocoding", "Locality found: " + address.getAddressLine(1) + "; state: " + state);
				} else {
					Log.d("Geocoding", "No matching addresses found.");
				}
			}
			else
			{
				Log.d("Geocoding", "Device not online or Geocoder not present.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
		}
		
		TwitchUtils.setGeocodingSuccess(p.a, success);
		TwitchUtils.setGeocodingStatus(p.a, true);
		return null;
	}
}
