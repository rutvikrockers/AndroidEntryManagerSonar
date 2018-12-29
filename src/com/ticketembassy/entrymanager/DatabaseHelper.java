package com.ticketembassy.entrymanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "entrymanager";
	private static final int DATABASE_VERSION = 1;
	private static DatabaseHelper dbHelper = null;
	private static SQLiteDatabase db = null;

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// storing the object of this class to dbHelper
		dbHelper = this;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		User.onCreate(db);
		Attendee.onCreate(db);
		Myevent.onCreate(db);
		Tickets.onCreate(db);
		//Log.e("test", "database created");
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		User.onUpgrade(db, oldVersion, newVersion);
		Attendee.onUpgrade(db, oldVersion, newVersion);
		Myevent.onUpgrade(db, oldVersion, newVersion);
		Tickets.onUpgrade(db, oldVersion, newVersion);
	}

	public Cursor executeQuery(String sql) {
		Cursor mCursor = db.rawQuery(sql, null);
		return mCursor;
	}


	public boolean executeDMLQuery(String sql) {
		try {
			db.execSQL(sql);
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public static DatabaseHelper getInstance(Context context) {
		if (dbHelper == null) {
			dbHelper = new DatabaseHelper(context);
			openConnecion();
		}
		return dbHelper;
	}

	private static void openConnecion() {
		if (db == null) {
			db = dbHelper.getWritableDatabase();
		}
	}


	public synchronized void closeConnecion() {
		if (dbHelper != null) {
			dbHelper.close();
			db.close();
			dbHelper = null;
			db = null;
		}
	}
}
