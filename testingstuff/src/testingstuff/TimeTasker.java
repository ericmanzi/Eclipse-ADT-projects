package testingstuff;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimeTasker {
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEEE");

	public static class TT extends TimerTask {
		@Override  
	    public void run() {  
	        // The logic of task/job that is going to be executed.  
	        // Here we are going to print the following string value  
	        System.out.println("This is being printed every 1 sec.");         
	    }
	}

	static long now = System.currentTimeMillis();

	public static Date getEndOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    return calendar.getTime();
	}

	public static Date getStartOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime();
	}
	static long DAY_IN_MILLISECONDS = 86400000;
	public static ArrayList<long[]> getDays(long goalStartDate, long currentDate) {
		ArrayList<long[]> days = new ArrayList<long[]>();
		long[] lastDay = {0, currentDate};
		if ((currentDate-goalStartDate)<86400000l) {
			lastDay[0]=getStartOfDay(new Date(currentDate)).getTime();
 		} else {
 			for (long start = goalStartDate; (start+DAY_IN_MILLISECONDS)<currentDate; start+=DAY_IN_MILLISECONDS) {
 				long end= start+DAY_IN_MILLISECONDS;
 				long[] daySpan = {start, end};
 				days.add(daySpan);
 				lastDay[0]=end; //set start of last day to end of last added day
 			}
 		}
		days.add(lastDay);
		return days;
	}
	
	public static void main(String[] args) {  
		Date nowDate = new Date(now);
		System.out.println(getStartOfDay(nowDate));
		System.out.println(getEndOfDay(nowDate));
		System.out.println(nowDate);
		
		ArrayList<long[]> days = getDays(System.currentTimeMillis()-86400001l, System.currentTimeMillis());
		for (long[] day: days) {
			System.out.println(sdf.format(day[0])+","+sdf.format(day[1]));
		}
        // Create an instance of our task/job for execution  
//		TT task = new TT();
          
        // We use a class java.util.Timer to   
        // schedule our task/job for execution  
//		Timer timer = new Timer();  
          
        // Let's schedule our task/job to be executed every 1 second  
//        timer.scheduleAtFixedRate(task, 0, 1000);  
        // First parameter: task - the job logic we   
        // created in run() method above.  
        // Second parameter: 0 - means that the task is   
        // executed in 0 millisecond after the program runs.  
        // Third parameter: 1000 - means that the task is   
        // repeated every 1000 milliseconds  
          
    }
}
