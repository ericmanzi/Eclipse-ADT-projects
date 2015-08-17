package com.example.detectandtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

/*
 * Adapter that is backed by an array of {@code DetectedActivity} objects. Finds UI elements in the
 * detected_activity layout and populates each element with data from a DetectedActivity
 * object.
 */
public class DetectedActivitiesAdapter extends ArrayAdapter<DetectedActivity> {
	public String currentActivity;
    public DetectedActivitiesAdapter(Context context,
                                     ArrayList<DetectedActivity> detectedActivities) {
        super(context, 0, detectedActivities);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DetectedActivity detectedActivity = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.detected_activity, parent, false);
        }

        // Find the UI widgets.
        TextView activityName = (TextView) view.findViewById(R.id.detected_activity_name);
        TextView activityConfidenceLevel = (TextView) view.findViewById(R.id.detected_activity_confidence_level);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.detected_activity_progress_bar);

        // Populate widgets with values.
        activityName.setText(Constants.getActivityString(getContext(),
                        detectedActivity.getType()));
        activityConfidenceLevel.setText(detectedActivity.getConfidence() + "%");
        progressBar.setProgress(detectedActivity.getConfidence());
        
        
        //start the timer
//        String act = Constants.getActivityString(getContext(), detectedActivity.getType());
//        if (act.equals("running") || act.equals("on foot") || act.equals("walking") ) {
//        	MainActivity.startTimer(view);
//        } else {
//        	MainActivity.stopTimer(view);
//        	MainActivity.saveRecord(MainActivity.spenttime);
//        }
//        
        return view;
    }

    /*
     * Process list of recently detected activities and updates the list of {@code DetectedActivity}
     * objects backing this adapter.
     *
     * @param detectedActivities the freshly detected activities
     */
    protected void updateActivities(ArrayList<DetectedActivity> detectedActivities) {
    	
        HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<Integer, Integer>();
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }
        currentActivity=getMostProbableActivity(detectedActivitiesMap);

        // Every time we detect new activities, we want to reset the confidence level of ALL
        // activities that we monitor. Since we cannot directly change the confidence
        // of a DetectedActivity, we use a temporary list of DetectedActivity objects. If an
        // activity was freshly detected, we use its confidence level. Otherwise, we set the
        // confidence level to zero.
        ArrayList<DetectedActivity> tempList = new ArrayList<DetectedActivity>();
        for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
            int confidence = detectedActivitiesMap.containsKey(Constants.MONITORED_ACTIVITIES[i]) ?
                    detectedActivitiesMap.get(Constants.MONITORED_ACTIVITIES[i]) : 0;

            tempList.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i],
                    confidence));
        }

        // Remove all items.
        this.clear();

        // Adding the new list items using {@link Adapter#addAll} notifies attached observers that
        // the underlying data has changed and views reflecting the data should refresh.
        this.addAll(tempList);
    }
    public String getActivityString() {
    	return currentActivity;
    }
    
    public String getMostProbableActivity(HashMap<Integer, Integer> detectedActivitiesMap) {
    	int maxProb = 0;
    	int maxProbActivity = 0;
    	for (int i: detectedActivitiesMap.keySet()) {
    		if (detectedActivitiesMap.get(i)>maxProb) {
    			maxProb=detectedActivitiesMap.get(i);
    			maxProbActivity=i;
    		}
    	}
    	return Constants.getActivityString(getContext(), maxProbActivity);
    }
    
}