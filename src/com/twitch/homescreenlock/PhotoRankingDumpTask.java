package com.twitch.homescreenlock;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

public class PhotoRankingDumpTask extends AsyncTask<Activity, Void, Void> {

	private void dumpToFile(Activity a) {
		TwitchDatabase db = new TwitchDatabase(a);
		db.open();
		db.dumpPhotoRankingToFile(a);
		db.close();
	}
	
	@Override
	protected Void doInBackground(Activity... arg0) {
		Activity a = arg0[0];
		Log.d("PhotoDump", "Started doing post in background");
		dumpToFile(a);
		
		if(TwitchUtils.isOnline(a)) {
			Log.d("PhotoDump", "is online, trying to push to server");
			String device = TwitchUtils.getDeviceID(a);
			String server_dir = "PhotoRanking/" + device;
			FTPClient client = new FTPClient();
			try {
				client.setControlKeepAliveTimeout(120);
				client.connect("twitch.stanford.edu", 21);
				client.login("kwyngarden", "passwould");
				client.makeDirectory("PhotoRanking/" + device);  
				
				String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/PhotoRankingDumps";
				Log.d("PhotoDump", "Looking for dumps in: " + dir_path);
				File dir = new File(dir_path);
				File[] dumps = dir.listFiles();
				for(File f : dumps) {
					FileInputStream fis = new FileInputStream(f);
					String path = server_dir + "/" + f.getName();
					Log.d("PhotoDump", "Sending " + f.getName() + " to " + path);
					client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			        client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			        client.storeFile(path, fis);
			        fis.close();
			        f.delete(); // Erase the file so it won't be sent again
				}
		        
				// Close connection and note update time
		        client.logout();
		        client.disconnect();
		        TwitchUtils.setLastDumpedPhotos(a);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("CogStudy", "Couldn't upload DB file: hit exception");
			}		    
		} else {
			Log.d("CogStudy", "Couldn't upload DB file: no internet connection");
		}
		
		
		return null;
	}

}
