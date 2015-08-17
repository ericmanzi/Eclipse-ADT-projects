package com.example.detectandtime;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;


/*
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService{
	protected static final String TAG = "activity-detection-intent-service";

	/*
	 * This constructor is required, and calls the super IntentService(String)
	 * constructor with the name for a worker thread.
	 */
	public DetectedActivitiesIntentService() {
		// Use the TAG to name the worker thread.
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
		Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
		// Get the list of the probable activities associated with the current state of the
		// device. Each activity is associated with a confidence level, which is an int between
		// 0 and 100.
		ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
		// Log each activity.
        Log.i(TAG, "activities detected");
        for (DetectedActivity da: detectedActivities) {
            Log.i(TAG, Constants.getActivityString(getApplicationContext(),da.getType()) + " " + da.getConfidence() + "%");
        }
     // Broadcast the list of detected activities.
        localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        //Broadcast the most probable activity
        localIntent.putExtra("MOST_PROBABLE", result.getMostProbableActivity());
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}

}
