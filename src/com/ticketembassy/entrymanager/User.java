package com.ticketembassy.entrymanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class User {
	
	public String email;
	public int user_id;
	private static final String CREATE_TABLE = "CREATE TABLE  `user` ("
			+ "`email` VARCHAR( 255 ) NOT NULL ,"
			+ "`user_id` int( 11 ) NOT NULL )";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(User.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
}
