package com.eric0322.detectandtimer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;


public class MainActivity extends Activity implements
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnClickListener {

	private Chronometer chronometer;
	//private ActivityRecognitionScan arclient;
	private String activitytext="";
	private GoogleApiClient mClient;
	private PendingIntent callbackIntent;
	private TextView activityTV;
	private IntentFilter mBroadcastFilter;
	private LocalBroadcastManager mBroadcastManager;
	private Intent intent;
	private Receiver receiver;
	private String gotActivity;
	public Handler myHandler = new Handler();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		((Button) findViewById(R.id.start_btn)).setOnClickListener(this);
		((Button) findViewById(R.id.stop_btn)).setOnClickListener(this);
		startNotification();
		activityTV = (TextView) findViewById(R.id.acttxt);
		
	}
	
	public void startNotification() {
		Intent i=new Intent(this, NotificationService.class);
		startService(i);
	}

	public void stopNotification() {
		stopService(new Intent(this, NotificationService.class));
	}

	BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			gotActivity = intent.getStringExtra("Activity");
			String v = "Activity:" + gotActivity + "\n";
			v += activitytext;
			activitytext=v;
			activityTV.setText(activitytext);
			Log.d("Activity", activitytext);
		}
	};
	
	@Override
	public void onStart() {
		super.onStart();
		
		intent = new Intent(this, ActivityRecognitionService.class);

		int resp=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if(resp == ConnectionResult.SUCCESS){
			Log.d("Response","google play online");
			mClient = new GoogleApiClient.Builder(this)
			.addApi(ActivityRecognition.API)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
			mClient.connect();
		}
//		 myHandler.post(new Runnable(){
//		        @Override
//		        public void run() {
//		        	activityTV.setText(Log.getStackTraceString(null));
//		            myHandler.postDelayed(this,500); 
//		        }
//		    });
		 
		//		receiver = new Receiver();
		//		IntentFilter filter = new IntentFilter();
		//		filter.addAction("ACTIVITY_RECOGNITION_DATA");
		//		registerReceiver(receiver, filter);
		//		activityTV.setText(receiver.getActivityText());

		activityTV.setText("started");
		// Set the broadcast receiver intent filer
		mBroadcastManager = LocalBroadcastManager.getInstance(this);
		// Create a new Intent filter for the broadcast receiver
		mBroadcastFilter = new IntentFilter("ACTIVITY_RECOGNITION_DATA");
		mBroadcastFilter.addCategory("ACTIVITY_RECOGNITION_DATA");
		
		//register receiver
		registerReceiver(updateListReceiver, mBroadcastFilter);
		Log.d("Response","registered receiver");
	}

	

	//	@Override
	//	public void onResume() {
	//		IntentFilter filter = new IntentFilter();
	//		filter.addAction("ACTIVITY_RECOGNITION_DATA");
	//		registerReceiver(receiver, filter);
	//	}
	//	@Override 
	//	public void onPause() {
	//		unregisterReceiver(receiver);
	//	}

		@Override
		public void onDestroy() {
			stopNotification();
			super.onDestroy();
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.start_btn:
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.start();
			break;
		case R.id.stop_btn:
			chronometer.stop();
			break;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d("Connection", "connection failed");
		activityTV.setText("Connection Failed"+arg0.toString());
	}

	@Override
	public void onConnected(Bundle arg0) {
		intent.setAction("ActivityRecognitionService");
		Log.d("Connection", "connection succesful");
		callbackIntent = PendingIntent.getService(MainActivity.this, (int)Math.random(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient, 20, callbackIntent);  
	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

}