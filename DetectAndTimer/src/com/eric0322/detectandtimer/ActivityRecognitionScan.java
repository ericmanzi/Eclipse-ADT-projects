package com.eric0322.detectandtimer;

import android.app.IntentService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityRecognitionScan implements 
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private Context context;
	private static final String TAG = "ActivityRecognition";
	private static GoogleApiClient mActivityRecognitionClient;
	private static PendingIntent callbackIntent;
	
	
	public ActivityRecognitionScan(Context context) {
		this.context=context;
	}
	
	public void startActivityRecognitionScan(){
		mActivityRecognitionClient	= new GoogleApiClient.Builder(this.context)
				.addApi(ActivityRecognition.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
            	.build();
		mActivityRecognitionClient.connect();
		Log.d(TAG, "startActivityRecognitionScan");
		//if(Config.TESTER_VERSION){Log.d(TAG,"startActivityRecognitionScan");}
		}
	public void stopActivityRecognitionScan() {
		try {
		ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mActivityRecognitionClient, callbackIntent);
		mActivityRecognitionClient.disconnect();
		Log.d(TAG,"stopActivityRecognitionScan");
		} catch (IllegalStateException e){
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d(TAG, "stopActivityRecogntionScan");
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(TAG, "ConnectionSuccesful");
		Intent intent = new Intent(context, ActivityRecognitionService.class);
		callbackIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mActivityRecognitionClient, 20, callbackIntent);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
