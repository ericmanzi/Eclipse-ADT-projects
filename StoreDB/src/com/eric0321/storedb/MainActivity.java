package com.eric0321.storedb;

import java.text.DateFormat;
import java.util.Date;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnClickListener{
	private Chronometer chronos;
	private String record="";
	private TextView recordedTimes;
	private long startTime;
	private String formattedDate="";
//	private WakeLock wakeLock;
	SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		chronos = (Chronometer) findViewById(R.id.chronometer);
		((Button) findViewById(R.id.start_btn)).setOnClickListener(this);
		((Button) findViewById(R.id.stop_btn)).setOnClickListener(this);
		recordedTimes = ((TextView) findViewById(R.id.recordedTimes));
		record = preferences.getString("recordText", "");
		recordedTimes.setText(record);
		//keep the CPU running in order to complete some work before the device goes to sleep
//		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//		        "MyWakelockTag");
//		wakeLock.acquire();
//		startService(new Intent(this, NotificationService.class));
	}

//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		wakeLock.release();
//	    stopService(new Intent(this, NotificationService.class));
//	}
	
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
	public static String convertMillisecondsToHMmSs(long milliseconds) {
		long seconds = milliseconds/1000;
	    long s = seconds % 60;
	    long m = (seconds / 60) % 60;
	    long h = (seconds / (60 * 60)) % 24;
	    return String.format("%d:%02d:%02d", h,m,s);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.start_btn:
			startTime = System.currentTimeMillis();
			formattedDate = DateFormat.getDateTimeInstance().format(new Date(startTime));
			long elapsed = preferences.getLong("pausetime", 0);
			chronos.setBase(SystemClock.elapsedRealtime()+elapsed);
			chronos.start();
			break;
		case R.id.stop_btn:
			long spenttime=SystemClock.elapsedRealtime()-chronos.getBase();
			chronos.stop();
			saveRecord(spenttime);
			break;
		case R.id.pausebtn:
			long spentTimeAtPause=chronos.getBase()-SystemClock.elapsedRealtime();
			chronos.stop();
			preferences.edit().putLong("pausetime", spentTimeAtPause).commit();
			break;
		}
	}
	
	public void startViewProgress(View view) {
		  Intent intent = new Intent(this, ViewProgress.class);
		  startActivity(intent);
	}
	public void startEditLogs(View view) {
		Intent intent = new Intent(this, EditLogs.class);
		startActivity(intent);
	}
	
	/*
	 * Records recorded time as <LONG starttime, LONG duration> in SharedPreferences and updates textview
	 * @param spenttime
	 */
	public void saveRecord(long spenttime) {
		String hms = convertMillisecondsToHMmSs(spenttime);
		record = "\n"+formattedDate+"_____"+hms+record;
		recordedTimes.setText(record);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(String.valueOf(startTime), spenttime);//save the starttime (long string) and duration (long) milliseconds
		editor.putString("recordText", record);
		editor.commit();

	}
	
}


