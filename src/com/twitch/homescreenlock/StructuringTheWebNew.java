package com.twitch.homescreenlock;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class StructuringTheWebNew extends Activity{

	int x0,x1,x2,y;
	View STWcontent, feedbackRight, feedbackWrong;
	LinearLayout STWcontentWrap,overall;
	HomeFeatureLayout hsv;
	float initialPositionX, initialPositionY, finalPositionX,finalPositionY;

	// Data structure to hold STW questions
	String[] extraction;
	String sourceSentence, topic;
	TextView textSentence, textExtraction, header;
	final String COLOR_TOPIC = "#556270";
	final String COLOR_RELATION = "#C44D58";
	final String COLOR_OBJECT = "#4A948F";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_structuring_the_web_new);
		
		STWcontentWrap=(LinearLayout)findViewById(R.id.STWnew_contentWrap);
		STWcontent=(View)findViewById(R.id.STWnew_content);
		feedbackRight=(View)findViewById(R.id.STWnew_feedbackRight);
		feedbackWrong=(View)findViewById(R.id.STWnew_feedbackWrong);
		hsv = (HomeFeatureLayout)findViewById(R.id.STWnew_contentScrollView);
		
        setViewWidth();	    
        
        //snapping scroll
        ArrayList<View> viewList = new ArrayList<View>();
        viewList.add(feedbackRight);
        viewList.add(STWcontent);
        viewList.add(feedbackWrong);
        hsv.setFeatureItems(viewList);
        
        //set text
		textSentence = (TextView) findViewById(R.id.STWnew_sentence);
		textExtraction = (TextView) findViewById(R.id.STWnew_extraction);
       
		PackageManager pm = getPackageManager(); //Enables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		
		
		extraction = getTabSeparatedExtraction();
//		if(extraction != null) {
//			sourceSentence = extraction[11];
//			topic = extraction[12];
//			header.setText(extraction[12]);
//			setSentenceTextFormatted(extraction);
//			setExtractionTextFormatted(extraction);
//		} // If error in file, will display noncolored Stanford founded extraction
             
	}

	private String[] getTabSeparatedExtraction() {
		try {
			AssetManager assetManager = getAssets();
			if (assetManager != null){
				
			String fileName = "StructuringTheWeb.txt";
			final int numLines = getNumExtractionLines(assetManager, fileName);
			InputStream is = assetManager.open(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			// Read desired line from file
			int row = TwitchUtils.getSTWrow(this);
			Log.d("Twitch_STW", "Grabbing extraction from row: " + row);
			for(int i = 0; i < row; i++) {
				br.readLine(); // Skip over previous rows
			}
			String line = br.readLine();
			br.close();
			
			// Update prefs and return line split around tab
			// Skip 1-100 rows ahead
			row += 1 + ((int) 100 * Math.random());
			if(row >= numLines) row = row % numLines;
			Log.d("Twitch_STW", "Skipped ahead to row: " + row);
			TwitchUtils.setSTWrow(this, row);
			String[] splitExtraction = line.split("\\t");
			return splitExtraction;
			} else {
				Log.d("getasset","null");
				return null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private int getNumExtractionLines(AssetManager manager, String fileName) {
		try {
			InputStream is = manager.open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			int lines = 0;
			while (reader.readLine() != null) lines++;
			reader.close();
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private void setSentenceTextFormatted(String[] values) {
		String sentence = values[11];
		String[] words = sentence.split(" ");
		int startTopic = Integer.parseInt(values[4]);
		int endTopic = Integer.parseInt(values[5]);
		int startRelation = Integer.parseInt(values[6]);
		int endRelation = Integer.parseInt(values[7]);
		int startObject = Integer.parseInt(values[8]);
		int endObject = Integer.parseInt(values[9]);
				
		StringBuilder formatted = new StringBuilder("");
		// Add words before topic
		addToFormatted(formatted, words, 0, startTopic);
		// Add topic
		formatted.append("<font color='" + COLOR_TOPIC + "'>");
		addToFormatted(formatted, words, startTopic, endTopic);
		formatted.append("</font>");
		// Add words between topic and relation
		addToFormatted(formatted, words, endTopic, startRelation);
		// Add relation
		formatted.append("<font color='" + COLOR_RELATION + "'>");
		addToFormatted(formatted, words, startRelation, endRelation);
		formatted.append("</font>");
		// Add words between relation and object
		addToFormatted(formatted, words, endRelation, startObject);
		// Add object
		formatted.append("<font color='" + COLOR_OBJECT + "'>");
		addToFormatted(formatted, words, startObject, endObject);
		formatted.append("</font>");
		// Add words after object
		addToFormatted(formatted, words, endObject, words.length);
		
		// Set formatted sentence text
		Log.d("Twitch_STW", "Setting sentence to: " + formatted.toString());
		textSentence.setText(Html.fromHtml(formatted.toString()));
	}
	
	private void addToFormatted(StringBuilder formatted, String[] words, int startIndex, int endIndex) {
		String punctuations = ".,:;?";
		for(int i = startIndex; i < endIndex; i++) {
			if(i == words.length - 1 || (words[i+1].length() == 1 && punctuations.contains(words[i+1]))) {
				formatted.append(StringEscapeUtils.escapeHtml4(words[i])); // Don't space around punctuation
			} else {
				formatted.append(StringEscapeUtils.escapeHtml4(words[i] + " "));
			}
		}
	}

	private void setExtractionTextFormatted(String[] values) {
		String formatted = "";
		formatted += "<font color='" + COLOR_TOPIC + "'>";
		formatted += StringEscapeUtils.escapeHtml4(values[1] + " ");
		formatted += "</font><font color='" + COLOR_RELATION + "'>";
		formatted += StringEscapeUtils.escapeHtml4(values[2] + " ");
		formatted += "</font><font color='" + COLOR_OBJECT + "'>";
		formatted += StringEscapeUtils.escapeHtml4(values[3]) + "</font>";
		Log.d("Twitch_STW", "Setting extraction to: " + formatted);
		textExtraction.setText(Html.fromHtml(formatted));
	}
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home_screen_lock, menu);
		return true;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);

		//initially scroll to the STWcontent
		x1 = STWcontent.getLeft();
		x0 = feedbackRight.getLeft();
		x2 = feedbackWrong.getLeft();
		y = STWcontent.getTop();
		hsv.scrollTo(x1, y);
	}
	
	public void setViewWidth() {
		// TODO Auto-generated method stub
//	    Display display = getWindowManager().getDefaultDisplay();
//	    Point size = new Point();
//	    display.getSize(size);
//	    int width = size.x;
//	    
//		DisplayMetrics metrics=getBaseContext().getResources().getDisplayMetrics();
		//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(metrics.widthPixels*3, metrics.heightPixels);
//	    overall=(LinearLayout)findViewById(R.id.overallLayout); 
//		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(metrics.widthPixels*3, metrics.heightPixels);

		
//		int width;
//	    LayoutParams paramsWrap=(LayoutParams) STWcontentWrap.getLayoutParams();
//	    paramsWrap.width=3*width;
	    //STWcontentWrap.setLayoutParams(params);
	    //overall.setLayoutParams(params);
//	    hsv.setLayoutParams(params);
//	    LayoutParams paramsContent=(LayoutParams) STWcontent.getLayoutParams();
//	    paramsContent.width=width;
//	    STWcontent.setLayoutParams(paramsWrap);
//	    
//	    LayoutParams paramsRight=(LayoutParams) feedbackRight.getLayoutParams();
//	    paramsRight.width=width;
//	    feedbackRight.setLayoutParams(paramsRight);	 
//	    
//	    LayoutParams paramsWrong=(LayoutParams) feedbackWrong.getLayoutParams();
//	    paramsWrong.width=width;
//	    feedbackWrong.setLayoutParams(paramsWrong);

	}
	
}
