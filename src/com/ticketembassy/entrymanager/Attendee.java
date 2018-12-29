package com.ticketembassy.entrymanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Attendee {
	public int id;
	public int checkin;
	public String created_at;
	public String email;
	public int event_id;
	public String first_name;
	public String image;
	public int quantity;
	public String last_name;
	public int sync_status;
	public String ticket_type;
	public String updated_at;
	
	private static final String CREATE_TABLE = "CREATE TABLE `attendee` ("
			+ "`id` int(11) NOT NULL,"
			+ "`checkin` BOOL NOT NULL DEFAULT 0,"
			+ "`created_at` varchar(255) NOT NULL,"
			+ "`email` varchar(255) NOT NULL,"
			+ "`event_id` int(11) NOT NULL,"
			+ "`quantity` int(11) NOT NULL,"
			+ "`first_name` varchar(255) NOT NULL,"
			+ "`image` varchar(255) NOT NULL,"
			+ "`last_name` varchar(255) NOT NULL," 
			+ "`sync_status` BOOL NOT NULL DEFAULT 0,"
			+ "`ticket_type` varchar(255) NOT NULL,"
			+ "`updated_at` varchar(255) NOT NULL,"
			+ "PRIMARY KEY  (`id`));";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(User.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS `attendee`");
		onCreate(database);
	}
}