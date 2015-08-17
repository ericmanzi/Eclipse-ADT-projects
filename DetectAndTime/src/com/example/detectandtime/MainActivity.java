package com.example.detectandtime;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends ActionBarActivity implements
ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>{
	private Chronometer chronometer;
	private Button startButton;
	private Button stopButton;
	private String record="";
	private TextView tvRecord;
	private TextView tvCurrentActivity;
	private String formattedDate;
	public long spenttime;
	private Button mRequestActivityUpdatesButton;
	private Button mRemoveActivityUpdatesButton;
	private ListView mDetectedActivitiesListView;
	private boolean timerRunning=false;
	private static final int DISCOVER_DURATION = 300;
	private static final int REQUEST_BLU = 1;



	protected static final String TAG = "activity-recognition";

	//  A receiver for DetectedActivity objects broadcast by the {@code ActivityDetectionIntentService}.
	protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;

	//  Provides the entry point to Google Play services.
	protected GoogleApiClient mGoogleApiClient;

	//  Used when requesting or removing activity detection updates.
	private PendingIntent mActivityDetectionPendingIntent;

	//  Adapter backed by a list of DetectedActivity objects.
	private DetectedActivitiesAdapter mAdapter;

	//  We use this to initialize the detectedActivityAdapter    
	private ArrayList<DetectedActivity> mDetectedActivities;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		startButton = ((Button) findViewById(R.id.start_btn));
		stopButton = ((Button) findViewById(R.id.stop_btn));
		tvRecord = (TextView) findViewById(R.id.tvRecord);
		tvCurrentActivity = (TextView) findViewById(R.id.tvCurrentActivity);

		//more UI objects
		mRequestActivityUpdatesButton = (Button) findViewById(R.id.request_activity_updates_button);
		mRemoveActivityUpdatesButton = (Button) findViewById(R.id.remove_activity_updates_button);
		mDetectedActivitiesListView = (ListView) findViewById(R.id.detected_activities_listview);


		// Get a receiver for broadcasts from ActivityDetectionIntentService.
		mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

		// Enable either the Request Updates button or the Remove Updates button depending on
		// whether activity updates have been requested.
		setButtonsEnabledState();

		// Reuse the value of mDetectedActivities from the bundle if possible. This maintains state
		// across device orientation changes. 
		if (savedInstanceState != null && savedInstanceState.containsKey(
				Constants.DETECTED_ACTIVITIES)) {
			mDetectedActivities = (ArrayList<DetectedActivity>) savedInstanceState.getSerializable(
					Constants.DETECTED_ACTIVITIES);
		} else {
			//If mDetectedActivities is not stored in the bundle,
			// populate it with DetectedActivity objects whose confidence is set to 0. Doing this
			// ensures that the bar graphs for only only the most recently detected activities are
			// filled in.

			mDetectedActivities = new ArrayList<DetectedActivity>();

			// Set the confidence level of each monitored activity to zero.
			for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
				mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
			}
		}

		mAdapter = new DetectedActivitiesAdapter(this, mDetectedActivities);
		mDetectedActivitiesListView.setAdapter(mAdapter);

		buildGoogleApiClient();

	}
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(ActivityRecognition.API)
		.build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Toast.makeText(this, "Device does not support bluetooth.",
					Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_STREAM, "testing");
			PackageManager pm = getPackageManager();
			List<ResolveInfo> appsList = pm.queryIntentActivities( intent, 0);
			if(appsList.size() > 0) {
				String packageName = null;
				String className = null;
				boolean found = false;

				for(ResolveInfo info: appsList){
					packageName = info.activityInfo.packageName;
					if( packageName.equals("com.android.bluetooth")){
						className = info.activityInfo.name;
						found = true;
						break;// found
					}
				}
				intent.setClassName(packageName, className);
				startActivity(intent);

				if(! found){
					Toast.makeText(this, "Bluetooth not found",
							Toast.LENGTH_SHORT).show(); // exit
				}
			}
			enableBlu();
			startActivity(intent);

		}


	}
	public void enableBlu(){
		// enable device discovery - this will automatically enable Bluetooth
		Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

		discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
				DISCOVER_DURATION );

		startActivityForResult(discoveryIntent, REQUEST_BLU);

	}
	// When startActivityForResult completes...
	protected void onActivityResult (int requestCode,
			int resultCode,
			Intent data) {

		if (resultCode == DISCOVER_DURATION
				&& requestCode == REQUEST_BLU) {

			// processing code goes here
		}
		else{ // cancelled or error
			Toast.makeText(this, "Bluetooth connection cancelled",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Register the broadcast receiver that informs this activity of the DetectedActivity
		// object broadcast sent by the intent service.
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
				new IntentFilter(Constants.BROADCAST_ACTION));
	}

	@Override
	protected void onPause() {
		// Unregister the broadcast receiver that was registered during onResume().
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		super.onPause();
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

	public void startTimer(View v) {start();}
	private void start() {
		if (!timerRunning) {
			chronometer.setBase(SystemClock.elapsedRealtime());
			formattedDate = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
			chronometer.start();
			timerRunning=true;
		}

	}
	public void stopTimer(View v) {stop();}
	private void stop() {
		if (timerRunning) {
			spenttime=SystemClock.elapsedRealtime()-chronometer.getBase();
			chronometer.stop();
			timerRunning=false;
			saveRecord(spenttime);
		}
	}

	public void saveRecord(long spenttime) {
		String hms = convertMillisecondsToHMmSs(spenttime);
		record = "\n"+formattedDate+"_____"+hms+record;
		tvRecord.setText(record);
	}

	public String convertMillisecondsToHMmSs(long milliseconds) {
		long seconds = milliseconds/1000;
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;
		return String.format("%d:%02d:%02d", h,m,s);
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + arg0.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "Connected to GoogleApiClient");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}
	/*
	 * Registers for activity recognition updates using
	 * Since this activity implements the PendingResult interface, the activity itself receives the 
	 * callback, and the code within {@code onResult} executes.
	 *  Note: once {@code requestActivityUpdates()} completes
	 * successfully, the {@code DetectedActivitiesIntentService} starts receiving call-backs when
	 * activities are detected.
	 */
	public void requestActivityUpdatesButtonHandler(View view) {
		if (!mGoogleApiClient.isConnected()) {
			Toast.makeText(this, "Google api client not yet connected. try again",
					Toast.LENGTH_SHORT).show();
			return;
		}
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
				mGoogleApiClient,
				Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
				getActivityDetectionPendingIntent()
				).setResultCallback(this);
	}
	//like requestActivityupdates 
	public void removeActivityUpdatesButtonHandler(View view) {
		if (!mGoogleApiClient.isConnected()) {
			Toast.makeText(this, "Google api client not yet connected. try again",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// Remove all activity updates for the PendingIntent that was used to request activity
		// updates.
		ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
				mGoogleApiClient,
				getActivityDetectionPendingIntent()
				).setResultCallback(this);
	}

	/*
	 * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
	 * available. Either method can complete successfully or with an error.
	 */
	@Override
	public void onResult(Status status) {
		if (status.isSuccess()) {
			// Toggle the status of activity updates requested, and save in shared preferences.
			//            boolean requestingUpdates = !getUpdatesRequestedState();
			//            setUpdatesRequestedState(requestingUpdates);

			// Update the UI. Requesting activity updates enables the Remove Activity Updates
			// button, and removing activity updates enables the Add Activity Updates button.
			//            setButtonsEnabledState();
			// Toggle the status of activity updates requested, and save in shared preferences.
			boolean requestingUpdates = !getUpdatesRequestedState();
			setUpdatesRequestedState(requestingUpdates);

			// Update the UI. Requesting activity updates enables the Remove Activity Updates
			// button, and removing activity updates enables the Add Activity Updates button.
			setButtonsEnabledState();

			Toast.makeText(
					this,
					requestingUpdates ? "activity updates added" :
						"activity updates added",
						Toast.LENGTH_SHORT
					).show();

		} else {
			Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
		}
	}

	/*
	 * Gets a PendingIntent to be sent for each activity detection.
	 */
	private PendingIntent getActivityDetectionPendingIntent() {
		// Reuse the PendingIntent if we already have it.
		if (mActivityDetectionPendingIntent != null) {
			return mActivityDetectionPendingIntent;
		}
		Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
		// requestActivityUpdates() and removeActivityUpdates().
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}


	/*
	 * Ensures that only one button is enabled at any time. The Request Activity Updates button is
	 * enabled if the user hasn't yet requested activity updates. The Remove Activity Updates button
	 * is enabled if the user has requested activity updates.
	 */
	private void setButtonsEnabledState() {
		if (getUpdatesRequestedState()) {
			mRequestActivityUpdatesButton.setEnabled(false);
			mRemoveActivityUpdatesButton.setEnabled(true);
		} else {
			mRequestActivityUpdatesButton.setEnabled(true);
			mRemoveActivityUpdatesButton.setEnabled(false);
		}
	}

	/*
	 * Retrieves a SharedPreference object used to store or read values in this app. If a
	 * preferences file passed as the first argument to {@link #getSharedPreferences}
	 * does not exist, it is created when {@link SharedPreferences.Editor} is used to commit
	 * data.
	 */
	private SharedPreferences getSharedPreferencesInstance() {
		return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
	}

	/*
	 * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
	 * updates.
	 */
	private boolean getUpdatesRequestedState() {
		return getSharedPreferencesInstance()
				.getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
	}

	/*
	 * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
	 * updates.
	 */
	private void setUpdatesRequestedState(boolean requestingUpdates) {
		getSharedPreferencesInstance()
		.edit()
		.putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
		.commit();
	}
	/*
	 * Stores the list of detected activities in the Bundle.
	 */
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putSerializable(Constants.DETECTED_ACTIVITIES, mDetectedActivities);
		super.onSaveInstanceState(savedInstanceState);
	}


	/*
	 * Checks to see if current activity is 'moving' and starts the time.
	 * Processes the list of freshly detected activities. Asks the adapter to update its list of
	 * DetectedActivities with new {@code DetectedActivity} objects reflecting the latest detected
	 * activities.
	 */
	protected void updateDetectedActivitiesList(ArrayList<DetectedActivity> detectedActivities) {
		mAdapter.updateActivities(detectedActivities);
		String act=mAdapter.getActivityString();
		tvCurrentActivity.setText(act);
		if (	act.equals("in vehicle") ||
				act.equals("on bicycle") ||
				act.equals("on foot") ||
				act.equals("walking")) {
			start();
		} else if (	act.equals("tilting") || 
				act.equals("still")) {
			stop();
		}
	}

	//     Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
	//     Receives a list of one or more DetectedActivity objects associated with the current state of
	//     the device.
	public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
		protected static final String TAG = "activity-detection-response-receiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<DetectedActivity> updatedActivities =
					intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
			updateDetectedActivitiesList(updatedActivities);
		}

	}

}
