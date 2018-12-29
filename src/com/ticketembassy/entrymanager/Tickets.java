package com.ticketembassy.entrymanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Tickets {
	
	public int id;
	public int sync_status;
	public String ticket_no;
	public int event_id;
	
	private static final String CREATE_TABLE = "CREATE TABLE `tickets` ("
			+ "`id` int(11) NOT NULL PRIMARY KEY,"
			+ "`sync_status` BOOL NOT NULL DEFAULT 1,"
			+ "`event_id` int(11),"
			+ "`ticket_no` varchar(255) NOT NULL);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(User.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS `tickets`");
		onCreate(database);
	}
}
