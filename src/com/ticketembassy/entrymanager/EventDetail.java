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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.rockers.ticketing.R;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import Pojo.MyEventDetailPojo;
import Pojo.MyEventPojo;
import api.ApiClient;
import api.RestInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetail extends SherlockActivity {
	private OnClickListener checkin_listner;
	private Myevent eventdetail;
	private int event_id;
	private RefreshAsync refresh;
	private TextView event_title;
	private TextView event_timing;
	private TextView event_vanue;
	private TextView tickets_sold;
	private TextView ticketsvailable;
	private TextView attendee_checkin;
	private TextView attendee_total;
	private ProgressBar progressbar1;
	private ProgressBar progressbar2;
	boolean asyncrun;
	private RestInterface restInterface;
	public EventDetail() {
		eventdetail = new Myevent();
		checkin_listner = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),EventAttendee.class);
				intent.putExtra("param", event_id);
				startActivity(intent);
			}
		};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		event_id = Integer.parseInt(getIntent().getStringExtra("event_id"));
		setContentView(R.layout.activity_event_detail);
		setTitle(R.string.event_info);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		((Button) findViewById(R.id.checkin)).setOnClickListener(checkin_listner);
		progressbar1 = (ProgressBar) findViewById(R.id.progressBar1);
		progressbar2 = (ProgressBar) findViewById(R.id.progressBar2);
		event_title = (TextView) findViewById(R.id.event_title);
		event_timing = (TextView) findViewById(R.id.event_timing);
		event_vanue = (TextView) findViewById(R.id.event_vanue);
		tickets_sold = (TextView) findViewById(R.id.tickets_sold);
		ticketsvailable = (TextView) findViewById(R.id.ticketsvailable);
		attendee_checkin = (TextView) findViewById(R.id.attendee_checkin);
		attendee_total = (TextView) findViewById(R.id.attendee_total);
		//refresh = new RefreshAsync();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fetchevent();
		updateview();
		//findViewById(R.id.menu_search).performClick();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.event_detail, menu);
		final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {

				restInterface = ApiClient.getClient().create(RestInterface.class);
				Call<MyEventDetailPojo> call = restInterface.EventRefresh(Constants.USER_ID,event_id);
		/*		call.enqueue(new Callback<MyEventDetailPojo>() {
					@Override
					public void onResponse(Call<MyEventDetailPojo> call, Response<MyEventDetailPojo> response) {
						if (response.body().isSuccess()) {
							updateview();
						//	update_event(Myevent);

							JSONObject jsobj = null;


							JSONObject temp = null;


							update_event(eventdetail);
						//	fetchevent();
							asyncrun = true;
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								invalidateOptionsMenu();
							}
						} else {
						//	ExecptionHandler.register(EventDetail.this,response.body().toString());
						}
					}

					@Override
					public void onFailure(Call<MyEventDetailPojo> call, Throwable t) {
                       t.printStackTrace();
					}
				});*/
				EventDetail.this.refresh = new RefreshAsync();
				EventDetail.this.refresh.execute((Void) null);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == android.R.id.home) {
			finish();

		} else if (i == R.id.menu_refresh) {
			asyncrun = false;
			item.setActionView(R.layout.indeterminate_progress_action);

		}
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
		if(asyncrun)
		{
			refresh.setActionView(null);
			asyncrun = false;
		}
		return true;
	}

	public void update_event(Myevent event) {
		//Log.e("test", event.id + "");
		DatabaseHelper
				.getInstance(EventDetail.this)
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

	public void fetchevent() {
		Cursor cursor = DatabaseHelper.getInstance(EventDetail.this)
				.executeQuery("select * from `myevent` where `id`=" + event_id);
		//Log.e("tes","select * from `myevent` where `id`=" + event_id);
		if (null != cursor) {
			cursor.moveToNext();
			eventdetail.vanue_name = cursor.getString(cursor
					.getColumnIndex("vanue_name"));
			eventdetail.id = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("id")));
			eventdetail.sync_status = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("sync_status")));
			eventdetail.ticket_no = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("ticket_no")));
			eventdetail.count_attendee = Integer.parseInt(cursor
					.getString(cursor.getColumnIndex("count_attendee")));
			eventdetail.count_attendee_count = Integer.parseInt(cursor
					.getString(cursor.getColumnIndex("count_attendee_count")));
			eventdetail.created_at = cursor.getString(cursor
					.getColumnIndex("created_at"));
			eventdetail.event_end_date_time = cursor.getString(cursor
					.getColumnIndex("event_end_date_time"));
			eventdetail.event_start_date_time = cursor.getString(cursor
					.getColumnIndex("event_start_date_time"));
			eventdetail.event_title = cursor.getString(cursor
					.getColumnIndex("event_title"));
			eventdetail.sold_tickets = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("sold_tickets")));
			eventdetail.total_tickets = Integer.parseInt(cursor
					.getString(cursor.getColumnIndex("total_tickets")));
			eventdetail.updated_at = cursor.getString(cursor
					.getColumnIndex("updated_at"));
			eventdetail.user_id = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("user_id")));
			//}
		}
		try {
			cursor.moveToFirst();
		}catch (NullPointerException e){
			e.printStackTrace();
		}

	}

	/*
	 * Async Task
	 */

	private class RefreshAsync extends AsyncTask<Void, Void, Object> {

		@Override
		protected void onPreExecute() {
			/*loading = new ProgressDialog(EventDetail.this);
			loading.setMessage(getResources().getString(R.string.event_details));
			loading.setIndeterminate(true);
			loading.setCancelable(true);
			loading.show();*/
		}

		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/myevent_details");
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
				Log.e("hii", _response);
				
				JSONObject jsobj = new JSONObject(_response);
				
				JSONObject temp = jsobj.getJSONObject("event");
//				JSONArray temp2 = jsobj.getJSONArray("count_checkin_attendee");
				
				Myevent myevent = new Myevent();
				
				myevent.id = Integer.parseInt(temp.getString("id"));
				myevent.sync_status = 0;
				myevent.ticket_no = 0;
				myevent.count_attendee = Integer.parseInt(temp.getString("count_attendee"));
				myevent.count_attendee_count = Integer.parseInt(temp.getString("count_checkin_attendee"));
				myevent.created_at = temp.getString("created_at");
				myevent.event_end_date_time = temp.getString("event_end_date_time");
				myevent.event_start_date_time = temp.getString("event_start_date_time");
				myevent.event_title = temp.getString("event_title");
				
				myevent.sold_tickets = Integer.parseInt(temp.getString("sold"));
				
				myevent.total_tickets = Integer.parseInt(temp.getString("count_of_ticket"));
				myevent.updated_at = (new Date()).toString();
				myevent.user_id = Integer.parseInt(Constants.USER_ID);
				myevent.vanue_name = temp.getString("vanue_name");
				update_event(myevent);
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
			//loading.dismiss();
			if (json instanceof JSONObject) {
				updateview();
				asyncrun = true;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					invalidateOptionsMenu();
				}
			} else {
				ExecptionHandler.register(EventDetail.this, (Exception) json);
			}
		}
	}
	
	public void updateview()
	{
		event_title.setText(eventdetail.event_title);
		event_timing.setText(getResources().getString(R.string.on) + " " + Constants.formatteddate(eventdetail.event_start_date_time));
		event_vanue.setText(Constants.formatteddateevent(eventdetail.event_start_date_time, eventdetail.event_end_date_time));
		tickets_sold.setText(eventdetail.sold_tickets+"");
		ticketsvailable.setText("/"+eventdetail.total_tickets+"");
		attendee_checkin.setText(eventdetail.count_attendee_count+"");
		attendee_total.setText("/"+eventdetail.count_attendee+"");
		//Log.e("test", (int)(((float)eventdetail.sold_tickets/eventdetail.total_tickets)*100) +"");
		//Log.e("test", (int)(((float)eventdetail.count_attendee_count/eventdetail.count_attendee)*100) +"");
		//progressbar1.getProgressDrawable().setColorFilter(Color.WHITE, Mode.MULTIPLY);
		//progressbar1.getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
		progressbar1.setProgress((int)(((float)eventdetail.sold_tickets/eventdetail.total_tickets)*100));
		progressbar2.setProgress((int)(((float)eventdetail.count_attendee_count/eventdetail.count_attendee)*100));
	}

}
