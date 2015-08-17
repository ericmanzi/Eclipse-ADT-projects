package com.eric0321.storedb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewProgress extends ActionBarActivity  {
	private LinearLayout linearLayoutText;
	private LinearLayout linearLayoutImage;
	private ImageView image;
	private int goalTime;
	private Date startDate;
	private Date endDate;
	private EditText goalTimeText;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private long newLogTime;
	private Date logStartDate;
	private EditText etLogTimeText;
	private DatePicker logStartDatePicker;
	private Button doneLog;
	private final long DAY_IN_MILLISECONDS = 86400000;
	SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_progress);
		linearLayoutText = (LinearLayout) findViewById(R.id.linearLayoutText);
		linearLayoutImage = (LinearLayout) findViewById(R.id.linearLayoutImages);
		goalTimeText = (EditText) findViewById(R.id.etGoalTime);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		goalTimeText.setHint(String.valueOf(preferences.getInt("goal time", 10)));
		startDatePicker = (DatePicker) findViewById(R.id.startDatePicker);
		endDatePicker = (DatePicker) findViewById(R.id.endDatePicker);
		logStartDatePicker = (DatePicker) findViewById(R.id.logStartDatePicker);
		etLogTimeText = (EditText) findViewById(R.id.etLogNewTime);
//		displayProgress(preferences);
		displayFakeProgress();
	    
	}
	
	
	public TextView makeText(String text) {
		TextView tv = new TextView(this);
		tv.setText(text);
		tv.setTextSize(15);
		tv.setHeight(240);
		return tv;
	}
	
	public ImageView makeImage(int identifier) {
		image = new ImageView(ViewProgress.this);
		image.setImageResource(identifier);
		image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        image.setAdjustViewBounds(true);
    	return image;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//saves the goal that is set in SharedPreferences with key "goal time" 
	//and start and end dates as LONG milliseconds since 1/1/1970
	public void saveGoal(View view) {
		String n0 = goalTimeText.getText().toString();
		goalTime= n0.equals("") ? 0 : Integer.parseInt(n0);
		startDate = getDateFrom(startDatePicker);
		endDate = getDateFrom(endDatePicker);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("goal time", goalTime);
		editor.putLong("goal start date", startDate.getTime());
		editor.putLong("goal end date", endDate.getTime());
		editor.commit();
	}
	//saves the time that is logged in SharedPreferences with the key as a string of the
	//start time in LONG milliseconds since 1/1/1970
	public void logIt(View view) {
		String n0 = etLogTimeText.getText().toString();
		newLogTime= n0.equals("") ? 0 : Long.parseLong(n0);
		newLogTime*=60000; //to milliseconds
		logStartDate = getDateFrom(logStartDatePicker);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(String.valueOf(logStartDate.getTime()), newLogTime);
		editor.commit();
	}

	public static Date getDateFrom(DatePicker datePicker){
	    int day = datePicker.getDayOfMonth();
	    int month = datePicker.getMonth();
	    int year =  datePicker.getYear();

	    Calendar calendar = Calendar.getInstance();
	    calendar.set(year, month, day);

	    return calendar.getTime();
	}
	
	
	/*
	 * get the goal set for the week. get the current day of the week. get the start date of the week.
	 * sum up all logs that were entered each day: for all days from start of week to current day.
	 * map a map of day to cumulative number of logs that were made. use this map to plot days from 
	 */
	public void displayProgress(SharedPreferences preferences) {
		int goalTime = preferences.getInt("goal time", 0); //goaltime in minutes
		long goalStartDate = preferences.getLong("goal start date", 0); //goalstartdate in milliseconds
		long goalEndDate = preferences.getLong("goal end date", 0); //goalenddate in milliseconds
		long currentDate = System.currentTimeMillis(); //today's date in milliseconds
		ArrayList<long[]> days = getDays(goalStartDate, currentDate);
		ArrayList<String> dayNames = new ArrayList<String>();
		for (long[] day: days) {
			dayNames.add(sdf.format(new Date(day[0])));
		}
		ArrayList<Long> daysProgress = getDayProgessList(days, getDaysToActivityMap(days, preferences), goalTime);
		
		for(int day=days.size()-1;day>=0;day--) {//starting from today going back to start day
			long progress = daysProgress.get(day);
			if (0<=progress && progress<3) {
				linearLayoutImage.addView(makeImage(R.drawable.nest8));
			} if (3<=progress && progress<20) {
				linearLayoutImage.addView(makeImage(R.drawable.nest7));
			} if (20<=progress && progress<40) {
				linearLayoutImage.addView(makeImage(R.drawable.nest6));
			} if (40<=progress && progress<50) {
				linearLayoutImage.addView(makeImage(R.drawable.nest5));
			} if (50<=progress && progress<60) {
				linearLayoutImage.addView(makeImage(R.drawable.nest4));
			} if (60<=progress && progress<80) {
				linearLayoutImage.addView(makeImage(R.drawable.nest3));
			} if (80<=progress && progress<98) {
				linearLayoutImage.addView(makeImage(R.drawable.nest2));
			} if (progress>98) {
				linearLayoutImage.addView(makeImage(R.drawable.nest1));
			}
        	linearLayoutText.addView(makeText(dayNames.get(day)));
			
		}
	        
	}
	
	public void displayFakeProgress() {
		for (int i=8; i>=0; i--) {
			if (i==1) {
				linearLayoutImage.addView(makeImage(R.drawable.nest8));
			} if (i==2) {
				linearLayoutImage.addView(makeImage(R.drawable.nest7));
			} if (i==3) {
				linearLayoutImage.addView(makeImage(R.drawable.nest6));
			} if (i==4) {
				linearLayoutImage.addView(makeImage(R.drawable.nest5));
			} if (i==5) {
				linearLayoutImage.addView(makeImage(R.drawable.nest4));
			} if (i==6) {
				linearLayoutImage.addView(makeImage(R.drawable.nest3));
			} if (i==7) {
				linearLayoutImage.addView(makeImage(R.drawable.nest2));
			} if (i==8) {
				linearLayoutImage.addView(makeImage(R.drawable.nest1));
			}
        	linearLayoutText.addView(makeText("Day: "+i));
		}
	}
	
	//returns a list of days as pairs of start and end times
	public ArrayList<long[]> getDays(long goalStartDate, long currentDate) {
		ArrayList<long[]> days = new ArrayList<long[]>();
		long[] lastDay = {0, currentDate};
		for (long start = goalStartDate; (start+DAY_IN_MILLISECONDS)<currentDate; start+=DAY_IN_MILLISECONDS) {
			long end= start+DAY_IN_MILLISECONDS;
			long[] daySpan = {start, end};
			days.add(daySpan);
			lastDay[0]=end; //set start of last day to end of last added day
		}
		days.add(lastDay);
		return days;
	}
	//returns a map from day to sum duration of activity in that day
	public HashMap<String, Long> getDaysToActivityMap(ArrayList<long[]> days, SharedPreferences preferences) {
		HashMap<String, Long> dayToActivity = new HashMap<String, Long>();
		HashMap<String, Long> prefMap = (HashMap<String, Long>) preferences.getAll();
		for (long[] day: days) { //for each day
			for (String key:prefMap.keySet()) { //for each key in preferences
				if (key.substring(0, 1).equals("1")) { //if key is a date
					long time = Long.parseLong(key);
					if (day[0]<=time && time<=day[1]) { //then activity happened on this day, so add it to the dayToActivityMap
						String dayName = sdf.format(new Date(time));
						if (dayToActivity.containsKey(dayName)) { //if it already exists, add the time to existing
							dayToActivity.put(dayName, dayToActivity.get(dayName)+prefMap.get(key));
						} else {//otherwise add a new time
							dayToActivity.put(dayName, prefMap.get(key));
						}
					}
				}
			}
		}
		return dayToActivity;
	}
	
	//returns a list of percentages of the goal for each day's cumulative progress
	public ArrayList<Long> getDayProgessList(ArrayList<long[]> days, HashMap<String, Long> dayToActivity, int goalTime) {
		ArrayList<Long> daysProgress = new ArrayList<Long>();
		long cumProgress=0;
		for (long[] day: days) {
			String dayName = sdf.format(new Date(day[0]));
			long daysActivity=dayToActivity.get(dayName);
			cumProgress+=daysActivity;
			daysProgress.add(cumProgress*100/goalTime);
		}
		return daysProgress;
	}
}
