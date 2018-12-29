package com.ticketembassy.entrymanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import Pojo.MyEventPojo;
import Pojo.ResponseUserLogin;
import api.ApiClient;
import api.RestInterface;
import helpers.CallWSTask;
import helpers.GetJSONListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NewApi")
public class Dashboard extends SherlockActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
	private ListView listview;
	private EventsAdapter eventsadapter;
	private ProgressDialog loading;
	private LoginAsync loginaync;
	private OnItemClickListener listener;
	List<Myevent> myevents;
	private RestInterface restInterface;
	List<Myevent> myevents_searchable;
	Map<String, String> params;
	private static final int RESULT_CLOSE_ALL = 0;
	private CallWSTask callWSTask;
	boolean asyncrun;

	public Dashboard() {
		listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(getApplicationContext(),	EventDetail.class);
				intent.putExtra("event_id", myevents.get(arg2).id + "");
				startActivity(intent);
			}
		};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (loading != null)
		{
			if(loading.isShowing())
			{
				loading.dismiss();
			}
		}
		setContentView(R.layout.activity_dashboard);
		setTitle(R.string.my_events);
		params = new HashMap<String, String>();
		myevents = new ArrayList<Myevent>();
		myevents_searchable = new ArrayList<Myevent>();
		listview = (ListView) findViewById(R.id.events);
		eventsadapter = new EventsAdapter();
		loginaync = new LoginAsync();
		loginaync.execute((Void) null);
	/*	if (loading == null)
			loading = new ProgressDialog(Dashboard.this);
		loading.setMessage(getResources().getString(R.string.my_events));
		loading.setIndeterminate(true);
		loading.setCancelable(true);
		loading.show();*/
		params.put("id",Constants.USER_ID);

	//	Follow_URL = getString(R.string.CRAWDED_ENVIRONMENT) + getString(R.string.API_FOLLOW_USER_URL, UserID, ProfileUseriD);
		String url ="http://mydesichef.com/ticketingsoft/mobile_logins/myevent?id=" + Constants.USER_ID ;
		//fetchevent();
		restInterface = ApiClient.getClient().create(RestInterface.class);

		listview.setAdapter(eventsadapter);
		listview.setOnItemClickListener(listener);
	//	fetchevent();
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				//loginaync = null;
				//fetchevent();
			//	deleteUnexpected();
				params.put("id",Constants.USER_ID);
				restInterface = ApiClient.getClient().create(RestInterface.class);
				Call<MyEventPojo> call = restInterface.Myevent(Constants.USER_ID);

				loginaync = new LoginAsync();
				loginaync.execute((Void) null);
				return false;
			}
		});
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		searchView.setOnQueryTextListener(this);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		// searchManager.startSearch(oldval, selectInitialQuery, self, null, false);
		// searchView.setIconifiedByDefault(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		/*
		 * Toast.makeText(this, "You searched for: " + query, Toast.LENGTH_LONG)
		 * .show();
		 */
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		find_search(newText);
		return false;
	}

	public void find_search(String str) {
		myevents_searchable.clear();
		for (int i = 0; i < myevents.size(); i++) {
			Myevent temp = myevents.get(i);
			if ((String.format("%s %s %s", temp.event_title, temp.vanue_name,
					Constants.formatteddate(temp.event_start_date_time)).trim()
					.toLowerCase(Locale.getDefault())).contains(str.trim()
					.toLowerCase(Locale.getDefault()))) {
				myevents_searchable.add(temp);
			}
		}
		eventsadapter.notifyDataSetChanged();
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == R.id.settings) {
			Intent intent = new Intent(getApplicationContext(), Settings.class);
			startActivityForResult(intent, RESULT_CLOSE_ALL);

		} else if (i == R.id.menu_refresh) {
			asyncrun = false;
			item.setActionView(R.layout.indeterminate_progress_action);

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

	public void onDestroy() {
		super.onDestroy();
		if (loading != null)
			if (loading.isShowing()) {
				loading.dismiss();
			}

	}

	/*
	 * Async Task
	 */

	private class LoginAsync extends AsyncTask<Void, Void, Object> {

		@Override
		protected void onPreExecute() {
			if (loading == null)
				loading = new ProgressDialog(Dashboard.this);
			loading.setMessage(getResources().getString(R.string.my_events));
			loading.setIndeterminate(true);
			loading.setCancelable(true);
			loading.show();
		}

		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/myevent");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("id",	Constants.USER_ID));
				nameValuePairs.add(new BasicNameValuePair("imei", Constants.IMEI));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				 Log.e("test",_response);
				JSONObject jsobj = new JSONObject(_response);
				insert_events(jsobj);
				deleteUnexpected();
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
			loading.dismiss();
			if (json instanceof JSONObject) {
				asyncrun = true;
				eventsadapter.notifyDataSetChanged();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					invalidateOptionsMenu();
				}
			} else {
				ExecptionHandler.register(Dashboard.this, (Exception) json);
			}
		}
	}

	/*
	 * Data Adapter
	 */

	private class EventsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return myevents_searchable.size();
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
				LayoutInflater inflater = (LayoutInflater) Dashboard.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				vi = inflater.inflate(R.layout.events_row, null);
			}
			Myevent event = myevents_searchable.get(position);
			((TextView) vi.findViewById(R.id.event_title))
					.setText(event.event_title);
			((TextView) vi.findViewById(R.id.event_timing)).setText(String
					.format("%s %s %s %s", getResources()
							.getString(R.string.on), Constants
							.formatteddate(event.event_start_date_time),
							getResources().getString(R.string.at),
							event.vanue_name));
			return vi;
		}
	}

	public void insert_events(JSONObject json) {
			try {
			for (int i = 0; i < json.getJSONArray("all_events").length(); i++) {
				JSONObject temp = json.getJSONArray("all_events")
						.getJSONObject(i);
				Myevent myevent = new Myevent();
				myevent.id = Integer.parseInt(temp.getString("id"));
				myevent.sync_status = 0;
				myevent.ticket_no = 0;
				myevent.count_attendee = Integer.parseInt(temp.getString(
						"count_attendee"));
				myevent.count_attendee_count = Integer.parseInt(temp
						.getString("count_checkin_attendee"));
				myevent.created_at = temp.getString("created_at");
				myevent.event_end_date_time = temp
						.getString("event_end_date_time");
				myevent.event_start_date_time = temp
						.getString("event_start_date_time");

				myevent.event_title = temp.getString("event_title");
				myevent.sold_tickets = Integer.parseInt(temp.getString(
						"sold"));
				myevent.total_tickets = Integer.parseInt(temp.getString(
						"count_of_ticket"));
				myevent.updated_at = (new Date()).toString();
				myevent.user_id = Integer.parseInt(Constants.USER_ID);
				myevent.vanue_name = temp.getString("vanue_name");
				if (isExists(myevent.id)) {
					update_event(myevent);
				} else {
					insert_event(myevent);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_CLOSE_ALL:
			setResult(RESULT_CLOSE_ALL);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void insert_event(Myevent event) {

		/*
		 * Log.e("test", String.format(
		 * "Insert into `myevent` (`id`,`sync_status`,`ticket_no`,`count_attendee`,`count_attendee_count`,`created_at`,`event_end_date_time`,`event_start_date_time`,`event_title`,`sold_tickets`,`total_tickets`,`updated_at`,`user_id`,`vanue_name`)"
		 * + " values (%d,%d,%d,%d,%d,'%s','%s','%s','%s',%d,%d,'%s',%d,'%s')",
		 * event.id, event.sync_status, event.ticket_no, event.count_attendee,
		 * event.count_attendee_count, event.created_at,
		 * event.event_end_date_time, event.event_start_date_time,
		 * event.event_title, event.sold_tickets, event.total_tickets,
		 * event.updated_at, event.user_id, event.vanue_name));
		 */
		event.event_title = event.event_title.replaceAll("'","");
		DatabaseHelper
				.getInstance(Dashboard.this)
				.executeDMLQuery(
						String.format(
								"Insert into `myevent` (`id`,`sync_status`,`ticket_no`,`count_attendee`,`count_attendee_count`,`created_at`,`event_end_date_time`,`event_start_date_time`,`event_title`,`sold_tickets`,`total_tickets`,`updated_at`,`user_id`,`vanue_name`)"
										+ " values (%d,%d,%d,%d,%d,'%s','%s','%s','%s',%d,%d,'%s',%d,'%s')",
								event.id, event.sync_status, event.ticket_no,
								event.count_attendee,
								event.count_attendee_count, event.created_at,
								event.event_end_date_time,
								event.event_start_date_time, event.event_title,
								event.sold_tickets, event.total_tickets,
								event.updated_at, event.user_id,
								event.vanue_name));
	}

	public void update_event(Myevent event) {

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
		event.event_title = event.event_title.replaceAll("'","");
		DatabaseHelper
				.getInstance(Dashboard.this)
				.executeDMLQuery(
						String.format(
								"Update `myevent` set `sync_status`=%d,`ticket_no`=%d,`count_attendee`=%d,"
										+ "`count_attendee_count`=%d,`created_at`='%s',`event_end_date_time`='%s',`event_start_date_time`='%s',"
										+ "`event_title`='%s',`sold_tickets`=%d,`total_tickets`=%d,`updated_at`='%s',`user_id`=%d,`vanue_name`='%s' where `id`=%d",
								event.sync_status, event.ticket_no,
								event.count_attendee,
								event.count_attendee_count, event.created_at,
								event.event_end_date_time,
								event.event_start_date_time, event.event_title,
								event.sold_tickets, event.total_tickets,
								event.updated_at, event.user_id,
								event.vanue_name, event.id));
	}

	public Boolean isExists(int id) {
		Cursor cursor = DatabaseHelper.getInstance(Dashboard.this)
				.executeQuery("select * from `myevent` where `id`=" + id);
		if (null != cursor) {
			while (cursor.moveToNext()) {

			}
		}
		cursor.moveToFirst();
		return false;
	}

	@SuppressWarnings("unchecked")
	public void fetchevent() {
		myevents.clear();
		Cursor cursor = DatabaseHelper.getInstance(Dashboard.this)
				.executeQuery(
						"select * from `myevent` where `user_id`=" + Constants.USER_ID);
		if (null != cursor) {
			while (cursor.moveToNext()) {
				Myevent event = new Myevent();
				event.id = cursor.getInt(cursor.getColumnIndex("id"));
				event.sync_status = cursor.getInt(cursor
						.getColumnIndex("sync_status"));
				event.ticket_no = cursor.getInt(cursor
						.getColumnIndex("ticket_no"));
				event.count_attendee = cursor.getInt(cursor
						.getColumnIndex("count_attendee"));
				event.count_attendee_count = cursor.getInt(cursor
						.getColumnIndex("count_attendee"));
				event.created_at = cursor.getString(cursor
						.getColumnIndex("created_at"));
				event.event_end_date_time = cursor.getString(cursor
						.getColumnIndex("event_end_date_time"));
				event.event_start_date_time = cursor.getString(cursor
						.getColumnIndex("event_start_date_time"));
				event.event_title = cursor.getString(cursor
						.getColumnIndex("event_title"));
				event.sold_tickets = cursor.getInt(cursor
						.getColumnIndex("count_attendee"));
				event.total_tickets = cursor.getInt(cursor
						.getColumnIndex("count_attendee"));
				event.updated_at = cursor.getString(cursor
						.getColumnIndex("updated_at"));
				event.user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
				event.vanue_name = cursor.getString(cursor
						.getColumnIndex("vanue_name"));
				myevents.add(event);
			}
		}
		myevents_searchable = (List<Myevent>) ((ArrayList<Myevent>) myevents).clone();
		cursor.moveToFirst();
	}

	public void deleteUnexpected() {
		String str = new String();
		for (int i = 0; i < myevents.size(); i++) {
			Myevent event = myevents.get(i);
			Log.e("test",event.id+"");
			//str.concat(event.id + ",");
			str += event.id + ",";
		}
		if (str.length() > 0) {
			str = str.substring(0, str.length() - 1);
			Cursor cursor = DatabaseHelper.getInstance(Dashboard.this)
					.executeQuery(
							"select * from `myevent` where `id` not in (" + str	+ ")");
			Log.e("test",str+"");
			if (null != cursor) {
				while (cursor.moveToNext()) {
					DatabaseHelper.getInstance(Dashboard.this)
							.executeDMLQuery(
									"delete from `myevent` where `id`="
											+ cursor.getInt(cursor
													.getColumnIndex("id")));
				}
			}
		}
	}

	@Override
	public boolean onSuggestionClick(int position) {
		return false;
	}

}
