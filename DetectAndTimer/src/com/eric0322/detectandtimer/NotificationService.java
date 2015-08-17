package com.eric0322.detectandtimer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Notification note=new Notification.Builder(this)
						.setContentTitle("JogCall is running")
						.setContentText(String.valueOf(System.currentTimeMillis()))
						.setSmallIcon(R.drawable.app_icon)
						.build();
		Log.d("NewNotification", "Created notification");
		Intent i=new Intent(this, MainActivity.class);
		PendingIntent pi=PendingIntent.getActivity(this, 0,i, 0);
		note.flags|=Notification.FLAG_NO_CLEAR;
		startForeground(1337, note);
		return(START_NOT_STICKY);
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
	}

}
