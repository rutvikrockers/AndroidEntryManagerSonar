package com.ticketembassy.entrymanager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.rockers.ticketing.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public final class Constants {
	public static final String BASE_URLs = "http://mydesichef.com/ticketingsoft_development/";


	public static String IMEI;
	public static String USER_ID;
	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
	
	public static boolean isOnline(Context context) { 
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();    
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	public static String formatteddate_deprected(String strdate)
	{
		
		try {
			//String format = "yyyy-MM-dd HH:mm";
			//Log.e("try date",strdate);
			//SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
			//return sdf.format(strdate);
			
			Date tempDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).parse(strdate);
			DateFormat df = new SimpleDateFormat("MMM,dd yyyy",Locale.US);
			return df.format(tempDate);
		} catch (ParseException e) {
			Log.e("exception date","exception date");
			e.printStackTrace();
		}
		return "";
		
	}
	
	public static String formatteddate(String strdate){
		try {
			Log.e("try date",strdate);
			String strCurrentDate = strdate;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date newDate = format.parse(strCurrentDate);

			format = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
			String date = format.format(newDate);
			return date;
			//Date tempDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).parse(strdate);
			//DateFormat df = new SimpleDateFormat("MMM,dd yyyy",Locale.US);
			//return df.format(tempDate);
		} catch (Exception e) {
			Log.e("exception date","exception date");
			e.printStackTrace();
		}
		return "";
	}
	
	public static String formatteddateevent(String startdate,String enddate)
	{
		try {
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startDate = format.parse(startdate);
			Date EndDate = format.parse(enddate);
			format = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatht = new SimpleDateFormat("HH:mma");
			
			if(format.format(startDate).equals(format.format(EndDate)))
			{
				return "From " + formatht.format(startDate) + " To " + formatht.format(EndDate);
			} 
			return "From " + format.format(startDate) + " To " + format.format(EndDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
		
	}
	
	public static String formatteddateevent_deprected(String startdate,String enddate)
	{
		try {
			Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).parse(startdate);
			Date endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).parse(enddate);
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd",Locale.US);
			SimpleDateFormat formate = new SimpleDateFormat("MMM,dd yyyy HH:mma",Locale.US);
			SimpleDateFormat format = new SimpleDateFormat("HH:mma",Locale.US);
			if(fmt.format(startDate).equals(fmt.format(endDate)))
			{
				return "From " + format.format(startDate) + " To " + format.format(endDate);
			} 
			return "From " + format.format(startDate) + " To " + formate.format(endDate);
			//return df.format(tempDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
		
	}
	
	public static String getTimeAgo(long time, Context ctx) {
		if (time < 1000000000000L) {
			// if timestamp given in seconds, convert to millis
			time *= 1000;
		}

		long now = System.currentTimeMillis();
		if (time > now || time <= 0) {
			return null;
		}

		final long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return String.format(Locale.getDefault(),"%s",ctx.getResources().getString(R.string.just_now));
		} else if (diff < 2 * MINUTE_MILLIS) {
			return String.format(Locale.getDefault(),"%s",ctx.getResources().getString(R.string.a_min_ago));
		} else if (diff < 50 * MINUTE_MILLIS) {
			return String.format(Locale.getDefault(),"%d %s",(diff / MINUTE_MILLIS) ,ctx.getResources().getString(R.string.minutes_ago));
		} else if (diff < 90 * MINUTE_MILLIS) {
			return String.format(Locale.getDefault(),"%s",ctx.getResources().getString(R.string.an_hour_ago));
		} else if (diff < 24 * HOUR_MILLIS) {
			return String.format(Locale.getDefault(),"%s",diff / HOUR_MILLIS,ctx.getResources().getString(R.string.hours_ago));
		} else if (diff < 48 * HOUR_MILLIS) {
			return String.format(Locale.getDefault(),"%s",ctx.getResources().getString(R.string.yesterday));
		} else {
			return String.format(Locale.getDefault(),"%d %s",(diff / DAY_MILLIS),ctx.getResources().getString(R.string.days_ago));
		}
	}
}