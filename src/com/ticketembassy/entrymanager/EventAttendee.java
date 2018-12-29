package com.ticketembassy.entrymanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.gson.Gson;
import com.rockers.ticketing.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import Pojo.AttendenceCheckinPojo;
import Pojo.MyEventDetailPojo;
import Pojo.UpdateAttendanceSinglePojo;
import api.ApiClient;
import api.RestInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NewApi")
public class EventAttendee extends SherlockActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
	public AttendeeAdapter adapter;
	private ListView listview;
	private List<Attendee> list;
	private ProgressDialog loading;
	private List<Attendee> attendee_searchable;
	private AttendeeAsync attendeeasync;
	int event_id;
	boolean asyncrun;
	private TextView footerhead;
	private Intent intentcamera;
	private RestInterface restInterface;
	private ProgressBar main_event_attendi;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		event_id = getIntent().getIntExtra("param", 0);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_event_attendee);
		list = new ArrayList<Attendee>();
		attendee_searchable = new ArrayList<Attendee>();
		listview = (ListView) findViewById(R.id.listView1);
		main_event_attendi= (ProgressBar)findViewById(R.id.main_event_attendi);
		footerhead = (TextView) findViewById(R.id.textView1);
		intentcamera = new Intent(this, CameraScannerActivity.class);
		adapter = new AttendeeAdapter();
		listview.setAdapter(adapter);
		footerhead.setText(String.format(getResources().getString(R.string.checkinfrom), 0));
		//fetchevent();
		attendeeasync = new AttendeeAsync();
		attendeeasync.execute((Void) null);

	}

	@Override
	public void onResume() {
		super.onResume();
		// Log.e("test", event_id + "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.event_attendee, menu);
		final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				attendeeasync = new AttendeeAsync();
				attendeeasync.execute((Void) null);

				restInterface = ApiClient.getClient().create(RestInterface.class);
				Call<AttendenceCheckinPojo> call = restInterface.AttendenceCheckin(Constants.USER_ID,event_id);

				return false;
			}
		});
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setOnQueryTextListener(this);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == android.R.id.home) {
			finish();

		} else if (i == R.id.menu_qrcode) {
			intentcamera.putExtra("param", event_id + "");
			startActivity(intentcamera);

		} else if (i == R.id.menu_refresh) {
			asyncrun = false;
			item.setActionView(R.layout.indeterminate_progress_action);

		} else if (i == R.id.menu_search) {
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
		if (asyncrun) {
			refresh.setActionView(null);
			asyncrun = false;
		}
		return true;
	}

	/*
	 * Async Task
	 */

	private class AttendeeAsync extends AsyncTask<Void, Void, Object> {

		@Override
		protected void onPreExecute() {
		/*	if (loading == null)
				loading = new ProgressDialog(EventAttendee.this);
			loading.setMessage(getResources()
					.getString(R.string.attendee_fetch));
			loading.setIndeterminate(true);
			loading.setCancelable(true);
			loading.show();*/
			main_event_attendi.setVisibility(View.VISIBLE);
		}

		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/attendee");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("id",
						Constants.USER_ID));
				nameValuePairs.add(new BasicNameValuePair("imei",
						Constants.IMEI));
				nameValuePairs.add(new BasicNameValuePair("event_id", event_id
						+ ""));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				// Log.e("test", _response);
				JSONObject jsobj = new JSONObject(_response);
				insert_events(jsobj);
				deleteUnexpected(jsobj);

				fetchevent();
				return jsobj;
			} catch (Exception e) {
				e.printStackTrace();
				return e;
			}
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Object json) {

			if (EventAttendee.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
				return;

			}
			main_event_attendi.setVisibility(View.GONE);
			/*loading.dismiss();
			loading = null;*/
			if (json instanceof JSONObject) {
				adapter.notifyDataSetChanged();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					invalidateOptionsMenu();
				}
				update_status();
			} else {
				ExecptionHandler.register(EventAttendee.this, (Exception) json);
			}
		}
	}

	/*
	 * Data Adapter
	 */

	class AttendeeAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (attendee_searchable.size() > 0) {
				EventAttendee.this.findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
			} else {
				EventAttendee.this.findViewById(R.id.textView2).setVisibility(
						View.VISIBLE);
			}
			return attendee_searchable.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) EventAttendee.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				vi = inflater.inflate(R.layout.single_attendee, null);
			}
			Attendee attendee = attendee_searchable.get(position);
			((TextView) vi.findViewById(R.id.attendee_name))
					.setText(attendee.first_name + " " + attendee.last_name
							+ " (" + getResources().getString(R.string.qty)
							+ ":" + attendee.quantity + ")");
			((TextView) vi.findViewById(R.id.attendee_title))
					.setText(attendee.email);
			vi.setId(position);
			// Log.e("test", vi.getId()+" " + position);
			if (attendee.checkin == 0) {
				uncheckinbutton(vi);
			} else {
				checkinbutton(vi);
			}
			return vi;
		}
	}

	public void insert_events(JSONObject json) {
		try {
			for (int i = 0; i < json.getJSONArray("msg").length(); i++) {
				JSONObject temp = json.getJSONArray("msg").getJSONObject(i);
				Attendee attendee = new Attendee();
				attendee.id = Integer.parseInt(temp.getString("id"));
				attendee.checkin = Integer.parseInt(temp
						.getString("checkin_status"));
				attendee.created_at = temp.getString("created_at");
				attendee.email = temp.getString("email");
				attendee.event_id = Integer
						.parseInt(temp.getString("event_id"));
				attendee.first_name = temp.getString("first_name");
				attendee.image = "";
				attendee.quantity = temp.getInt("ticket_qty");
				attendee.last_name = temp.getString("last_name");
				attendee.sync_status = 0;
				/*attendee.ticket_type = json.getJSONArray("ticket_type")
						.getString(i);*/
				attendee.updated_at = (new Date()).toString();
				if (isExists(attendee.id)) {
					update_event(attendee);
				} else {
					insert_event(attendee);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onStart() {
		//SharedPreferenceUtils o0bj= users.
		adapter = new AttendeeAdapter();
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		update_status();
		super.onStart();

	}
	public void insert_event(Attendee attendee) {

		/*
		 * Log.e("test", String.format(
		 * "Insert into `myevent` (`id`,`sync_status`,`ticket_no`,`count_attendee`,`count_attendee_count`,`created_at`,`event_end_date_time`,`event_start_date_time`,`event_title`,`sold_tickets`,`total_tickets`,`updated_at`,`user_id`,`vanue_name`)"
		 * + " values (%d,%d,%d,%d,%d,'%s','%s','%s','%s',%d,%d,'%s',%d,'%s')",
		 * event.id, event.sync_status, event.ticket_no, event.count_attendee,
		 * event.count_attendee_count, event.created_at,
		 * event.event_end_date_time, event.event_start_date_time,
		 * event.event_title, event.sold_tickets, event.total_tickets,
		 * event.updated_at, event.user_id, event.vanue_name)); Log.e("test",
		 * String.format(
		 * "Insert into `attendee` (`id`,`checkin`,`created_at`,`email`,`event_id`,`first_name`,`image`,`last_name`,`sync_status`,`ticket_type`,`updated_at`)"
		 * + " values (%d,%d,'%s','%s',%d,'%s','%s','%s',%d,'%s','%s')",
		 * attendee.id, attendee.checkin, attendee.created_at, attendee.email,
		 * attendee.event_id, attendee.first_name, attendee.image,
		 * attendee.last_name, attendee.sync_status, attendee.ticket_type,
		 * attendee.updated_at));
		 */
        attendee.first_name = attendee.first_name.replaceAll("'","");
        attendee.last_name = attendee.last_name.replaceAll("'","");
		DatabaseHelper
				.getInstance(EventAttendee.this)
				.executeDMLQuery(
						String.format(
								"Insert into `attendee` (`id`,`checkin`,`created_at`,`email`,`event_id`,`first_name`,`image`,`last_name`,`sync_status`,`ticket_type`,`updated_at`,`quantity`)"
										+ " values (%d,%d,'%s','%s',%d,'%s','%s','%s',%d,'%s','%s',%d)",
								attendee.id, attendee.checkin,
								attendee.created_at, attendee.email,
								attendee.event_id, attendee.first_name,
								attendee.image, attendee.last_name,
								attendee.sync_status, attendee.ticket_type,
								attendee.updated_at, attendee.quantity));
	}

	public void update_event(Attendee attendee) {

		/*
		 * Log.e("test", String.format(
		 * "Update `myevent` set `sync_status`=%d,`ticket_no`=%d,`count_attendee`=%d,"
		 * +
		 * "`count_attendee_count`=%d,`created_at`='%s',`event_end_date_time`='%s',`event_start_date_time`='%s',"
		 * +
		 * "`event_title`='%s',`sold_tickets`=%d,`total_tickets`=%d,`updated_at`='%s',`user_id`=%d,`vanue_name`='%s' where `id`=%d"
		 * , event.sync_status, event.ticket_no, event.count_attendee,
		 * event.count_attendee_count, event.created_at,
		 * event.event_end_date_time, event.event_start_date_time,
		 * event.event_title, event.sold_tickets, event.total_tickets,
		 * event.updated_at, event.user_id, event.vanue_name, event.id));
		 */
        attendee.first_name = attendee.first_name.replaceAll("'","''");
        attendee.last_name = attendee.last_name.replaceAll("'","''");
		DatabaseHelper
				.getInstance(EventAttendee.this)
				.executeDMLQuery(
						String.format(
								"Update `attendee` set `checkin`=%d,`email`='%s',`event_id`=%d,"
										+ "`first_name`='%s',`image`='%s',`last_name`='%s',`sync_status`=%d,`quantity`=%d,"
										+ "`ticket_type`='%s',`updated_at`='%s' where `id`=%d",
								attendee.checkin, attendee.email,
								attendee.event_id, attendee.first_name,
								attendee.image, attendee.last_name,
								attendee.sync_status, attendee.quantity,
								attendee.ticket_type, attendee.updated_at,
								attendee.id));
	}

	public void deleteUnexpected(JSONObject json) {
		// Log.e("test", "test");
		String str = new String();
		try {
			for (int i = 0; i < json.getJSONArray("msg").length(); i++) {
				JSONObject temp;
				temp = json.getJSONArray("msg").getJSONObject(i);
				str += temp.getString("id") + ",";
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// Log.e("test", str);
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
			/*
			 * Log.e("test", "select * from `attendee` where `id` not in ( str +
			 * ")");
			 */
			Cursor cursor = DatabaseHelper.getInstance(EventAttendee.this)
					.executeQuery(
							"select * from `attendee` where `id` not in ("
									+ str + ")");
			if (null != cursor) {
				while (cursor.moveToNext()) {
					DatabaseHelper.getInstance(EventAttendee.this)
							.executeDMLQuery(
									"delete from `attendee` where `id`="
											+ cursor.getInt(cursor
													.getColumnIndex("id")));
				}
			}
		}
	}

	public Boolean isExists(int id) {
		Cursor cursor = DatabaseHelper.getInstance(EventAttendee.this)
				.executeQuery("select * from `attendee` where `id`=" + id);
		if (null != cursor) {
			while (cursor.moveToNext()) {
				Log.e("exists", "fs");
				//return true;
			}
		}
		cursor.moveToFirst();
		return false;
	}

	public void update_status() {
		Cursor cursor = DatabaseHelper.getInstance(EventAttendee.this)
				.executeQuery(
						"select sum(`quantity`) as count from `attendee` where `event_id`="
								+ event_id + " and `checkin`=1");
		if (null != cursor) {
			cursor.moveToNext();
			footerhead.setText(String.format(
					getResources().getString(R.string.checkinfrom),
					cursor.getInt(cursor.getColumnIndex("count"))));
			DatabaseHelper
					.getInstance(EventAttendee.this)
					.executeDMLQuery(
							String.format(
									"update `myevent` set `count_attendee_count`=%d where `id`=%d",
									cursor.getInt(cursor
											.getColumnIndex("count")), event_id));
		} else {
			footerhead.setText(String.format(
					getResources().getString(R.string.checkinfrom), 0));
		}
	}

	@SuppressWarnings("unchecked")
	public void fetchevent() {
		list.clear();
		Cursor cursor = DatabaseHelper.getInstance(EventAttendee.this)
				.executeQuery(
						"select * from `attendee` where `event_id`=" + event_id
								+ "");
		if (null != cursor) {
			while (cursor.moveToNext()) {
				Attendee attendee = new Attendee();
				attendee.id = cursor.getInt(cursor.getColumnIndex("id"));
				attendee.checkin = cursor.getInt(cursor
						.getColumnIndex("checkin"));
				attendee.created_at = cursor.getString(cursor
						.getColumnIndex("created_at"));
				attendee.email = cursor.getString(cursor
						.getColumnIndex("email"));
				attendee.event_id = cursor.getInt(cursor
						.getColumnIndex("event_id"));
				attendee.first_name = cursor.getString(cursor
						.getColumnIndex(("first_name")));
				attendee.image = cursor.getString(cursor
						.getColumnIndex("image"));
				attendee.quantity = cursor.getInt(cursor
						.getColumnIndex("quantity"));
				attendee.last_name = cursor.getString(cursor
						.getColumnIndex("last_name"));
				attendee.sync_status = cursor.getInt(cursor
						.getColumnIndex("sync_status"));
				attendee.ticket_type = cursor.getString(cursor
						.getColumnIndex("ticket_type"));
				attendee.updated_at = (new Date()).toString();
				list.add(attendee);
			}
		}
		attendee_searchable = (List<Attendee>) ((ArrayList<Attendee>) list)
				.clone();
		cursor.moveToFirst();
	}

	public void checkinbutton(View v) {
		// Log.e("test",v.getParent().getClass().getName()+"");
	//	((LinearLayout) v).setBackgroundResource(R.drawable.cell_background);
		((Button) v.findViewById(R.id.checkin)).setText("");
		((Button) v.findViewById(R.id.checkin))
				.setBackgroundResource(R.drawable.checkdin);
		((Button) v.findViewById(R.id.checkin)).setEnabled(true);
		float alpha = 1f;
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		((Button) v.findViewById(R.id.checkin)).startAnimation(alphaUp);
		((Button) v.findViewById(R.id.checkin)).setOnClickListener(null);
		((Button) v.findViewById(R.id.checkin))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(EventAttendee.this);
						builder.setTitle("Check In Status");
						builder.setMessage("Are you sure want to uncheck #" + list.get(((LinearLayout) v.getParent()).getId()).id+"?");
						builder.setCancelable(false);
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// Log.e("test","mp");
								list.get(((LinearLayout) v.getParent()).getId()).sync_status = 1;
								list.get(((LinearLayout) v.getParent()).getId()).checkin = 0;
								v.setEnabled(false);
								update_event(list.get(((LinearLayout) v.getParent())
										.getId()));
								if (Constants.isOnline(EventAttendee.this)) {
									float alpha = 0.6f;
									AlphaAnimation alphaUp = new AlphaAnimation(alpha,
											alpha);
									alphaUp.setFillAfter(true);
									((Button) v).startAnimation(alphaUp);
									//(new CheckInAsync(v)).execute((Void) null);
									restInterface = ApiClient.getClient().create(RestInterface.class);
									Call<UpdateAttendanceSinglePojo> call = restInterface.UpdateAttendeeSingle(Constants.USER_ID,event_id,list
											.get(((LinearLayout) v.getParent()).getId()).id,list
											.get(((LinearLayout) v.getParent())
													.getId()).checkin);
									call.enqueue(new Callback<UpdateAttendanceSinglePojo>() {
										@Override
										public void onResponse(Call<UpdateAttendanceSinglePojo> call, Response<UpdateAttendanceSinglePojo> response) {
											v.setEnabled(true);
											if (response.body().isSuccess()) {
												try {
													//	if (response.body().isSuccess()) {
													list.get(((LinearLayout) v.getParent()).getId()).sync_status = 0;
													update_event(list.get(((LinearLayout) v
															.getParent()).getId()));
													//	}
													if (list.get(((LinearLayout) v.getParent()).getId()).checkin == 1) {
														uncheckinbutton((LinearLayout) v.getParent());
													} else {
														checkinbutton((LinearLayout) v.getParent());
													}

													adapter.notifyDataSetChanged();
													update_status();
												} catch (Exception e) {
													e.printStackTrace();
												}
											} else {
												// Log.e("test","stesdg");
												adapter.notifyDataSetChanged();
												update_status();
												//		ExecptionHandler.register(EventAttendee.this, (Exception) json);
											}
										}

										@Override
										public void onFailure(Call<UpdateAttendanceSinglePojo> call, Throwable t) {
											t.printStackTrace();
										}
									});

								} else {
									v.setEnabled(true);
									uncheckinbutton(v);
								}
							}
						});

						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						});

						builder.show();
					}
				});
	}

	public void uncheckinbutton(View v) {
		((LinearLayout) v).setBackgroundColor(Color.WHITE);
		((Button) v.findViewById(R.id.checkin)).setText(R.string.checkin);
		((Button) v.findViewById(R.id.checkin))
				.setBackgroundResource(R.drawable.check_in);
		((Button) v.findViewById(R.id.checkin)).setEnabled(true);
		((Button) v.findViewById(R.id.checkin)).setOnClickListener(null);
		float alpha = 1f;
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		((Button) v.findViewById(R.id.checkin)).startAnimation(alphaUp);
		((Button) v.findViewById(R.id.checkin))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(EventAttendee.this);
						builder.setTitle("Check In Status");
						builder.setMessage("Check In ticket #" + list.get(((LinearLayout) v.getParent()).getId()).id + "?");
						builder.setCancelable(false);
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// Log.e("test",((LinearLayout)v.getParent()).getId()+"");
								list.get(((LinearLayout) v.getParent()).getId()).sync_status = 1;
								list.get(((LinearLayout) v.getParent()).getId()).checkin = 1;
								v.setEnabled(false);
								update_event(list.get(((LinearLayout) v.getParent())
										.getId()));
								if (Constants.isOnline(EventAttendee.this)) {
									((Button) v).setText(R.string.loading);
									float alpha = 0.6f;
									AlphaAnimation alphaUp = new AlphaAnimation(alpha,
											alpha);
									alphaUp.setFillAfter(true);
									((Button) v).startAnimation(alphaUp);
									//		(new CheckInAsync(v)).execute((Void) null);
									restInterface = ApiClient.getClient().create(RestInterface.class);
									Call<UpdateAttendanceSinglePojo> call = restInterface.UpdateAttendeeSingle(Constants.USER_ID,event_id,list
											.get(((LinearLayout) v.getParent()).getId()).id,list
											.get(((LinearLayout) v.getParent())
													.getId()).checkin);
									call.enqueue(new Callback<UpdateAttendanceSinglePojo>() {
										@Override
										public void onResponse(Call<UpdateAttendanceSinglePojo> call, Response<UpdateAttendanceSinglePojo> response) {
											v.setEnabled(true);
											if (response.body().isSuccess()) {
												try {
													//	if (response.body().isSuccess()) {
													list.get(((LinearLayout) v.getParent()).getId()).sync_status = 0;
													update_event(list.get(((LinearLayout) v
															.getParent()).getId()));
													//	}
													if (list.get(((LinearLayout) v.getParent()).getId()).checkin == 1) {
														uncheckinbutton((LinearLayout) v.getParent());
													} else {
														checkinbutton((LinearLayout) v.getParent());
													}
													adapter.notifyDataSetChanged();
													update_status();
												} catch (Exception e) {
													e.printStackTrace();
												}
											} else {
												// Log.e("test","stesdg");
												adapter.notifyDataSetChanged();
												update_status();
												//		ExecptionHandler.register(EventAttendee.this, (Exception) json);
											}
										}
										@Override
										public void onFailure(Call<UpdateAttendanceSinglePojo> call, Throwable t) {
											t.printStackTrace();
										}
									});
								} else {
									v.setEnabled(true);
									checkinbutton(v);
								}
							}
						});

						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						});

						builder.show();
					}
				});
	}

	/*
	 * Async Task
	 */

	private class CheckInAsync extends AsyncTask<Void, Void, Object> {

		View position;

		CheckInAsync(View position) {
			this.position = position;
		}

		@Override
		protected Object doInBackground(Void... params) {
			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpClient httpclient = new DefaultHttpClient(httpParams);
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/update_attendee_single");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("id",
						Constants.USER_ID));
				nameValuePairs.add(new BasicNameValuePair("imei",
						Constants.IMEI));
				nameValuePairs.add(new BasicNameValuePair("event_id", event_id
						+ ""));
				nameValuePairs.add(new BasicNameValuePair("attendee_id", list
						.get(((LinearLayout) position.getParent()).getId()).id
						+ ""));
				nameValuePairs
						.add(new BasicNameValuePair("checkin_status", list
								.get(((LinearLayout) position.getParent())
										.getId()).checkin
								+ ""));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				 Log.e("test",nameValuePairs.toString());
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				 Log.e("test",_response);
				JSONObject jsobj = new JSONObject(_response);
				return jsobj;
			} catch (Exception e) {
				e.printStackTrace();
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object json) {
			// loading.dismiss();
			position.setEnabled(true);
			if (json instanceof JSONObject) {
				try {
					if (((JSONObject) json).getBoolean("success")) {
						list.get(((LinearLayout) position.getParent()).getId()).sync_status = 0;
						update_event(list.get(((LinearLayout) position
								.getParent()).getId()));
					}
					if (list.get(((LinearLayout) position.getParent()).getId()).checkin == 1) {
						uncheckinbutton((LinearLayout) position.getParent());
					} else {
						checkinbutton((LinearLayout) position.getParent());
					}
					adapter.notifyDataSetChanged();
					update_status();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				// Log.e("test","stesdg");
				adapter.notifyDataSetChanged();
				update_status();
				ExecptionHandler.register(EventAttendee.this, (Exception) json);
			}
		}
	}

	@Override
	public boolean onSuggestionClick(int position) {
		return false;
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		find_search(newText);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	public void find_search(String str) {
		attendee_searchable.clear();
		for (int i = 0; i < list.size(); i++) {
			Attendee temp = list.get(i);
			// Log.e("test", String.format("%s %s %s", temp.event_title,temp.vanue_name,Constants.formatteddate(temp.event_start_date_time)));
			if ((String.format(Locale.getDefault(), "%s %s %s %d",	temp.first_name, temp.last_name, temp.email, temp.id)
					.trim().toLowerCase(Locale.getDefault())).contains(str
					.trim().toLowerCase(Locale.getDefault()))) {
				attendee_searchable.add(temp);
			}
		}
		// Log.e("fsdf",""+myevents_searchable.size());
		adapter.notifyDataSetChanged();
	}

}
