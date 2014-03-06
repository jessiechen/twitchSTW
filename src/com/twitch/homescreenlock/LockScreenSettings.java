package com.twitch.homescreenlock;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LockScreenSettings extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.settings); 
		addPreferencesFromResource(R.xml.preferences); 
	}
}