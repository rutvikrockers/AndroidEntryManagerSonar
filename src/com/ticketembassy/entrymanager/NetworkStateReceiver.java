package com.ticketembassy.entrymanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		int status = NetworkUtil.getConnectivityStatus(context);
		if (status != 0) {
			List<Attendee> fetch = fetchevent(context);
			if (fetch.size() > 0) {
				(new CheckInAsync(fetch)).execute((Void) null);
			}
			List<Tickets> fet = fetchtickets(context);
			if (fet.size() > 0) {
				for (int i = 0; i < fet.size(); i++) {
					(new TicketsAsync(fet.get(i))).execute((Void) null);
				}
			}
		}
	}

	public List<Attendee> fetchevent(Context context) {
		Cursor cursor = DatabaseHelper.getInstance(context).executeQuery("select * from `attendee` where `sync_status` = 1");
		List<Attendee> list = new ArrayList<Attendee>();
		if (null != cursor) {
			while (cursor.moveToNext()) {
				//Log.e("test","test");
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
		return list;
	}

	public List<Tickets> fetchtickets(Context context) {
		Cursor cursor = DatabaseHelper.getInstance(context).executeQuery("select * from `attendee` where `sync_status` = 1");
		List<Tickets> list = new ArrayList<Tickets>();
		if (null != cursor) {
			while (cursor.moveToNext()) {
				Tickets attendee = new Tickets();
				attendee.id = cursor.getInt(cursor.getColumnIndex("id"));
				attendee.event_id = cursor.getInt(cursor.getColumnIndex("event_id"));
				attendee.sync_status = cursor.getInt(cursor.getColumnIndex("sync_status"));
				attendee.ticket_no = cursor.getString(cursor.getColumnIndex("ticket_no"));
				list.add(attendee);
			}
		}
		return list;
	}

	/*
	 * Async Task
	 */

	private class CheckInAsync extends AsyncTask<Void, Void, Object> {

		List<Attendee> position;

		CheckInAsync(List<Attendee> position) {
			this.position = position;
		}

		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/update_attendee_pull");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("id",
						Constants.USER_ID));
				nameValuePairs.add(new BasicNameValuePair("imei",
						Constants.IMEI));
				for (int i = 0; i < position.size(); i++) {
					nameValuePairs.add(new BasicNameValuePair("attendee_id[]",position.get(i).id + ""));
					nameValuePairs.add(new BasicNameValuePair("checkin_status[]", position.get(i).checkin + ""));
				}
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				//Log.e("test", nameValuePairs.toString());
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				Log.e("test", _response);
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
			update_view();
			if (json instanceof JSONObject) {
				// sync_status
				for (int i = 0; i < position.size(); i++) {
					position.get(i).sync_status = 0;
					update_event(position.get(i));
				}
			}
		}
	}

	public void update_event(Attendee attendee) {

		DatabaseHelper
				.getInstance(NetworkStateReceiver.this.context)
				.executeDMLQuery(
						String.format(
								"Update `attendee` set `checkin`=%d,`updated_at`='%s' where `id`=%d",
								attendee.checkin, attendee.updated_at,
								attendee.id));
	}

	/*
	 * Async Task
	 */

	private class TicketsAsync extends AsyncTask<Void, Void, Object> {
		Tickets data;

		TicketsAsync(Tickets data) {
			this.data = data;
		}

		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/ticket_check");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("id",
						Constants.USER_ID));
				nameValuePairs.add(new BasicNameValuePair("imei",
						Constants.IMEI));
				nameValuePairs.add(new BasicNameValuePair("event_id",
						data.event_id + ""));
				nameValuePairs.add(new BasicNameValuePair("ticket_number",
						data.ticket_no + ""));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				Log.e("test", nameValuePairs.toString());
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				Log.e("test", _response);
				JSONObject jsobj = new JSONObject(_response);
				return jsobj;
			} catch (Exception e) {
				e.printStackTrace();
				return e;
			}
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Object json) {
			if (json instanceof JSONObject) {
				try {
					for (int i = 0; i < ((JSONObject) json).getJSONArray("attendee_id").length(); i++) {
						updateifExists(Integer.parseInt(((JSONObject) json).getJSONArray("attendee_id").getString(i)));
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				update_view();
			}
		}
	}

	public Boolean updateifExists(int id) {
		if (DatabaseHelper.getInstance(NetworkStateReceiver.this.context)
				.executeDMLQuery(
						String.format(
								"Update `attendee` set `checkin`=%d,`sync_status`=%d,"
										+ "`updated_at`='%s' where `id`=%d", 1,
								0, (new Date()).toString(), id))) {
			return true;
		}
		return false;
	}

	public void update_view() {
		if (NetworkStateReceiver.this.context instanceof EventAttendee) {
			EventAttendee temp = ((EventAttendee) NetworkStateReceiver.this.context);
			temp.adapter.notifyDataSetChanged();
		}
	}

}
