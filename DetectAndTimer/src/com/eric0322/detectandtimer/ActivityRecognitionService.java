package com.eric0322.detectandtimer;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ActivityRecognitionService extends IntentService {
	private String TAG = "ActivityRecognition";
	public String activity="No activity";

	public ActivityRecognitionService() {
		super("ActivityRecognitionService");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, startId, startId);
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("Service", "Handling intent");
		if (ActivityRecognitionResult.hasResult(intent)) {
			Log.d("Service", "got result");
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity mostProbableActivity = result.getMostProbableActivity();
			int activityType = mostProbableActivity.getType();
			activity=getFriendlyName(activityType);
			Log.d(TAG, "ActivityRecognitionResult: "+activity);
			Log.d(TAG, result.toString()); 
			Intent broadcastIntent = new Intent(this, MainActivity.class);
			broadcastIntent.putExtra("Activity", activity);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
	}

	private static String getFriendlyName(int detected_activity_type){
		switch (detected_activity_type ) {
		case DetectedActivity.IN_VEHICLE:
			return "in vehicle";
		case DetectedActivity.ON_BICYCLE:
			return "on bike";
		case DetectedActivity.ON_FOOT:
			return "on foot";
		case DetectedActivity.TILTING:
			return "tilting";
		case DetectedActivity.STILL:
			return "still";
		case DetectedActivity.RUNNING:
			return "running";
		case DetectedActivity.WALKING:
			return "walking";
		default:
			return "unknown";
		}
	}
}
