package com.ticketembassy.entrymanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rockers.ticketing.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import Pojo.LogoutPojo;
import Pojo.MyEventDetailPojo;
import api.ApiClient;
import api.RestInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Settings extends SherlockActivity {
	private OnClickListener listner;
	private ProgressDialog loading;
	//private LogoutAsync logoutasync;
	private static final int RESULT_CLOSE_ALL = 0;
	private RestInterface restInterface;
	public Settings() {
		listner = new OnClickListener() {
			@Override
			public void onClick(View v) {
				(new AlertDialog.Builder(Settings.this))
						.setMessage(R.string.are_you_sure_)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										loading = new ProgressDialog(Settings.this);
										loading.setMessage(getResources().getString(R.string.logout_));
										loading.setIndeterminate(true);
										loading.setCancelable(true);
										loading.show();
										restInterface = ApiClient.getClient().create(RestInterface.class);
										Call<LogoutPojo> call = restInterface.Logout(Constants.USER_ID);
										call.enqueue(new Callback<LogoutPojo>() {
											@Override
											public void onResponse(Call<LogoutPojo> call, Response<LogoutPojo> response) {
												loading.dismiss();

											///	LogoutPojo = response.body();
											//	if (response.body()) {

													DatabaseHelper.getInstance(Settings.this).executeDMLQuery("DELETE from user;DELETE from tickets;DELETE from myevent;DELETE from attendee;");

													setResult(RESULT_CLOSE_ALL);
													startActivity(new Intent(Settings.this, Login.class));
													finish();
											//	} else {
												//	ExecptionHandler.register(Settings.this, (Exception) json);
											//	}
											}

											@Override
											public void onFailure(Call<LogoutPojo> call, Throwable t) {
                                                   t.printStackTrace();
											}
										});
									//	logoutasync = new LogoutAsync();
										//logoutasync.execute((Void) null);
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			}
		};
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		settitle();
		((Button) findViewById(R.id.logout)).setOnClickListener(listner);
	}
	public void settitle()
	{
		TextView txt = (TextView)findViewById(R.id.settings_email);
		Cursor cursor = DatabaseHelper.getInstance(Settings.this)
				.executeQuery("select * from `user`");
		//Log.e("tes","select * from `myevent` where `id`=" + event_id);
		if (null != cursor) {
			cursor.moveToNext();
			txt.setText(cursor.getString(cursor
					.getColumnIndex("email")));
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(5);
			finish();
			return true;
		}
		return true;
	}
	/*
	 * Async Task
	 */
	/*private class LogoutAsync extends AsyncTask<Void, Void, Object> {

		@Override
		protected void onPreExecute() {
			loading = new ProgressDialog(Settings.this);
			loading.setMessage(getResources().getString(R.string.logout_));
			loading.setIndeterminate(true);
			loading.setCancelable(true);
			loading.show();
		}
		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs	+ "mobile_logins/mobile_logout");
			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("id",
						Constants.USER_ID));
				nameValuePairs.add(new BasicNameValuePair("imei",
						Constants.IMEI));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				//Log.e("test", _response);
				//return jsonobject
				return new JSONObject(_response);
			} catch (Exception e) {
				e.printStackTrace();
				return e;
			}
		}
		@Override
		protected void onPostExecute(Object json) {
			loading.dismiss();
			if (json instanceof JSONObject) {
				DatabaseHelper.getInstance(Settings.this).executeDMLQuery("DELETE from user;DELETE from tickets;DELETE from myevent;DELETE from attendee;");
				*//*Intent intent = new Intent(Settings.this, Login.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
				finish();*//*
				setResult(RESULT_CLOSE_ALL);
				startActivity(new Intent(Settings.this, Login.class));
				finish();
			} else {
				ExecptionHandler.register(Settings.this, (Exception) json);
			}
		}
	}*/
}
