package com.ticketembassy.entrymanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Myevent {
	
	public int id;
	public int sync_status;
	public int ticket_no;
	public int count_attendee;
	public int count_attendee_count;
	public String created_at;
	public String event_end_date_time;
	public String event_start_date_time;
	public String event_title;
	public int sold_tickets;
	public int total_tickets;
	public String updated_at;
	public int user_id;
	public String vanue_name;
	
	private static final String CREATE_TABLE = "CREATE TABLE `myevent` ("
			+ "`id` int(11) NOT NULL,"
			+ "`sync_status` BOOL NOT NULL DEFAULT 0,"
			+ "`ticket_no` int(11) NOT NULL,"
			+ "`count_attendee` int(11) NOT NULL,"
			+ "`count_attendee_count` int(11) NOT NULL,"
			+ "`created_at` varchar(255) NOT NULL,"
			+ "`event_end_date_time` varchar(255) NOT NULL,"
			+ "`event_start_date_time` varchar(255) NOT NULL," 
			+ "`event_title` varchar(255) NOT NULL,"
			+ "`sold_tickets` int(11) NOT NULL,"
			+ "`total_tickets` int(11) NOT NULL,"
			+ "`updated_at` varchar(255) NOT NULL,"
			+ "`user_id` int(11) NOT NULL,"
			+ "`vanue_name` varchar(255) NOT NULL,"
			+ "PRIMARY KEY  (`id`));";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(User.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS `myevent`");
		onCreate(database);
	}
}