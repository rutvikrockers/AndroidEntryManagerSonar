package com.ticketembassy.entrymanager;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.rockers.ticketing.R;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;

public class Splash extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		final String android_id = Secure.getString(Splash.this.getContentResolver(), Secure.ANDROID_ID);
		Constants.IMEI = android_id;
		(new Handler()).post(new Runnable() {
			@Override
			public void run() {
				if (!fetchuser()) {
					finish();
					Intent intent = new Intent(getApplicationContext(), Login.class);
					startActivity(intent);
				} else {
					finish();
					Intent intent = new Intent(getApplicationContext(), Dashboard.class);
					startActivity(intent);
				}
			}
		});
	}

	private Boolean fetchuser() {
		Cursor cursor = DatabaseHelper.getInstance(Splash.this).executeQuery(
				"select * from `user`");
		if (null != cursor) {
			while (cursor.moveToNext()) {
				//Log.e("app", cursor.getString(cursor.getColumnIndex("email")));
				Constants.USER_ID = cursor.getString(cursor
						.getColumnIndex("user_id"));

			}
		}
		return false;
	}

}
