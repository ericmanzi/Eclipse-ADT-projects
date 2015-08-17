package com.eric0321.storedb;
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

		Log.d("NewNotification", "Created notification");
		Intent i=new Intent(this, MainActivity.class);
		PendingIntent pi=PendingIntent.getActivity(this, 0,i, 0);
		Notification note=new Notification.Builder(this)
				.setContentTitle("You're almost there!")
				.setContentText("You have completed X% of your goal")
				.setContentIntent(pi)
				.setSmallIcon(R.drawable.ic_launcher)
				.build();
		
		note.flags|=Notification.FLAG_NO_CLEAR;
		startForeground(1337, note);
		return(START_STICKY); //If Android does kill your service, it will be restarted ASAP
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
	}

}
