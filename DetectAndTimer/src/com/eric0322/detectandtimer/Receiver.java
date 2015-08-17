package com.eric0322.detectandtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
	public String activitytext = "";
	@Override
	public void onReceive(Context context, Intent intent) {
		if("ActivityRecognitionService".equals(intent.getAction().toString()))
		{
			String v = "Activity:" + intent.getStringExtra("Activity") + "\n";
			v += activitytext;
			activitytext=v;
			Log.d("Activity", activitytext);
		}
	}
	public String getActivityText() {
		return activitytext;
	}
}
