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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.rockers.ticketing.R;
import com.ticketembassy.entrymanager.CameraPreview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
//import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.widget.TextView;
/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

import Pojo.MyEventDetailPojo;
import Pojo.TicketCheckPojo;
import api.ApiClient;
import api.RestInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraTestActivity extends SherlockActivity {

	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	ImageScanner scanner;
	String eventid;
	ImageView imageviewmain;
	LinearLayout linearlayoutfooter;
	TextView title;
	TextView labelno;
	TextView labelago;
	ImageView status;
	private Date datenow;
	private String Data;
	Boolean asyncrun = true;
	List<Tickets> mytickets;
	private RestInterface restInterface;
	private boolean previewing = true;
	private int ticket;
	MenuItem refresh;
	static {
		System.loadLibrary("iconv");
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(R.string.scanner);
		setContentView(R.layout.main);
		mytickets = new ArrayList<Tickets>();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();
		eventid = getIntent().getStringExtra("param");
		title = (TextView) findViewById(R.id.textView1);
		imageviewmain = (ImageView) findViewById(R.id.imageView1);
		labelno = (TextView) findViewById(R.id.textView2);
		labelago = (TextView) findViewById(R.id.textView3);
		linearlayoutfooter = (LinearLayout) findViewById(R.id.linear_footer);
		status = (ImageView) findViewById(R.id.status);
		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.event_scanner, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == android.R.id.home) {
			finish();

		} else if (i == R.id.menu_search) {
		}
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		 refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
		if (asyncrun) {
			refresh.setActionView(null);
			refresh.setVisible(false);
			asyncrun = false;
		} else {
			refresh.setActionView(R.layout.indeterminate_progress_action);
			refresh.setVisible(true);
			asyncrun = true;
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		previewing = true;
		if (mCamera == null) {
			mCamera = getCameraInstance();
			mCamera.startPreview();
		}
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
		if (preview.getChildCount() > 0) {
			preview.removeAllViews();
		}
		preview.addView(mPreview);
		// Log.e("test","test");
	}

	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		@SuppressLint("NewApi")
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);
			int result = scanner.scanImage(barcode);
			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				SymbolSet syms = scanner.getResults();
				for (final Symbol sym : syms) {
					//Log.e("test ", sym.getData());
					datenow = new Date();
					asyncrun = false;
					invalidateOptionsMenu();

				//	(new CheckInAsync(sym.getData())).execute();
					String datas = null;


					DatabaseHelper
							.getInstance(CameraTestActivity.this)
							.executeDMLQuery(
									String.format(
											"Insert into tickets (`event_id`,`sync_status`,`ticket_no`) values(%s,1,'%s')",
											eventid, sym.getData()));
					Cursor crs = DatabaseHelper.getInstance(CameraTestActivity.this)
							.executeQuery("select max(id) as id from `tickets`");
					crs.moveToNext();
					ticket = crs.getInt(crs.getColumnIndex("id"));
					crs.moveToFirst();

					restInterface = ApiClient.getClient().create(RestInterface.class);
					Call<TicketCheckPojo> call = restInterface.TicketStatus(Constants.USER_ID,eventid,sym.getData());
					call.enqueue(new Callback<TicketCheckPojo>() {
						@Override
						public void onResponse(Call<TicketCheckPojo> call, final Response<TicketCheckPojo> response) {
							if (response.body().isSuccess()) {
								try {
//									if(response.body().getStatus()==2){
//
//									}

									if(response.body().getStatus()==0){
										releaseCamera();
										AlertDialog.Builder builder = new AlertDialog.Builder(CameraTestActivity.this);
										String TicketNumber = String.valueOf(response.body().getAttendee_id());
										builder.setTitle("Check In Status");
										builder.setMessage("Check In ticket #" + TicketNumber);
										builder.setCancelable(false);
										builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												restInterface = ApiClient.getClient().create(RestInterface.class);
												Call<TicketCheckPojo> call = restInterface.TicketCheck(Constants.USER_ID,eventid,sym.getData());
												call.enqueue(new Callback<TicketCheckPojo>() {
													@Override
													public void onResponse(Call<TicketCheckPojo> call, Response<TicketCheckPojo> response) {
														imageviewmain.setVisibility(View.GONE);
														linearlayoutfooter.setVisibility(View.VISIBLE);
														previewing = true;
														if (mCamera == null) {
															mCamera = getCameraInstance();
															mCamera.startPreview();
														}

														mPreview = new CameraPreview(CameraTestActivity.this, mCamera, previewCb, autoFocusCB);
														refresh.setActionView(null);
														refresh.setVisible(false);
														asyncrun = false;
														FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
														if (preview.getChildCount() > 0) {
															preview.removeAllViews();
														}
														preview.addView(mPreview);
														//	labelago.setText(((JSONObject) json).getString("message"));
														labelago.setText(response.body().getMessage());
														//	labelno.setText(((JSONObject) json).getString("attendee_id"));

														labelno.setText(Integer.toString(response.body().getAttendee_id()));
														status.setImageResource(R.drawable.true_);

														String str = new String("");
														String ids = new String("");

														/*DatabaseHelper
																.getInstance(CameraTestActivity.this)
																.executeDMLQuery(
																		String.format(
																				"Update `tickets` set `sync_status`=%d where `id`=%d",
																				0, ticket));
*/

														if (updateifExists((response.body().getAttendee_id()))) {

															//		Attendee attendee = fetchevent(Integer.parseInt(((JSONObject) json).getString("attendee_id")));
															Attendee attendee = fetchevent(response.body().attendee_id);

															str += attendee.first_name + ", ";
															ids += "#" + attendee.id + ",";

														}
														Attendee attendee = fetchevent(response.body().attendee_id);
														//str += "(" + attendee.ticket_type + ")";
														title.setText(str.toString());

													}

													@Override
													public void onFailure(Call<TicketCheckPojo> call, Throwable throwable) {

													}
												});

												if(response.body().getStatus()==2){



													status.setImageResource(R.drawable.delete);
													//	title.setText(((JSONObject) json).getString("message"));
													title.setText(response.body().getMessage());


												}else{


													//delete_view.setVisibility(View.VISIBLE);
													//linear_delete_view.setVisibility(View.VISIBLE);

												}

											}
										});

										builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												previewing = true;
												if (mCamera == null) {
													mCamera = getCameraInstance();
													mCamera.startPreview();
												}
												mPreview = new CameraPreview(CameraTestActivity.this, mCamera, previewCb, autoFocusCB);
												refresh.setActionView(null);
												refresh.setVisible(false);
												FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
												if (preview.getChildCount() > 0) {
													preview.removeAllViews();
												}
												preview.addView(mPreview);
											}
										});

										builder.show();

									}else if(response.body().getStatus()==2){
										releaseCamera();
										//previewing = false;
										status.setImageResource(R.drawable.delete);
										//	title.setText(((JSONObject) json).getString("message"));
										title.setText(response.body().getMessage());
										AlertDialog.Builder builder = new AlertDialog.Builder(CameraTestActivity.this);
										builder.setTitle("CheckIn");
										builder.setMessage("You are already checked-in to this event");
										builder.setCancelable(true);
										builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {

												previewing = true;
												if (mCamera == null) {
													mCamera = getCameraInstance();
													mCamera.startPreview();
												}
												mPreview = new CameraPreview(CameraTestActivity.this, mCamera, previewCb, autoFocusCB);
												refresh.setActionView(null);
												refresh.setVisible(false);
												FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
												if (preview.getChildCount() > 0) {
													preview.removeAllViews();
												}
												preview.addView(mPreview);
												//Toast.makeText(getApplicationContext(), "Neutral button clicked", Toast.LENGTH_SHORT).show();
											}
										});
										builder.show();
									}else {
										releaseCamera();
										AlertDialog.Builder builder = new AlertDialog.Builder(CameraTestActivity.this);
										builder.setTitle("CheckIn");
										builder.setMessage(response.body().getMessage());
										builder.setCancelable(true);
										builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												previewing = true;
												if (mCamera == null) {
													mCamera = getCameraInstance();
													mCamera.startPreview();
												}
												mPreview = new CameraPreview(CameraTestActivity.this, mCamera, previewCb, autoFocusCB);
												refresh.setActionView(null);
												refresh.setVisible(false);
												FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
												if (preview.getChildCount() > 0) {
													preview.removeAllViews();
												}
												preview.addView(mPreview);
												//Toast.makeText(getApplicationContext(), "Neutral button clicked", Toast.LENGTH_SHORT).show();
											}
										});
										builder.show();
									}


									mCamera.setPreviewCallback(previewCb);
									previewing = true;
									mCamera.startPreview();
									updateago();
									asyncrun = true;
									invalidateOptionsMenu();
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								//	ExecptionHandler.register(CameraTestActivity.this, (Exception) json);
							}
						}

						@Override
						public void onFailure(Call<TicketCheckPojo> call, Throwable t) {
							t.printStackTrace();
						}
					});
				}
			}
		}
	};

	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	/*
	 * Async Task
	 */

	@Override
	public void onBackPressed()
	{
		Log.e("back", "msg");
		super.onBackPressed();
//	    finish();  
	}

	public void updateago() {
		labelago.setText(Constants.getTimeAgo(datenow.getTime(), this));
	}

	public Boolean updateifExists(int id) {
		if (DatabaseHelper.getInstance(CameraTestActivity.this)
				.executeDMLQuery(
						String.format(
								"Update `attendee` set `checkin`=%d,`sync_status`=%d,"
										+ "`updated_at`='%s' where `id`=%d", 1,
								0, (new Date()).toString(), id))) {
			return true;
		}
		return false;
	}

	public Attendee fetchevent(int attendeeid) {
		Cursor cursor = DatabaseHelper.getInstance(CameraTestActivity.this)
				.executeQuery("select * from `attendee` where `id`=" + attendeeid + "");
		Attendee attendee = new Attendee();
		if (null != cursor) {
			while (cursor.moveToNext()) {
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
				attendee.updated_at = cursor.getString(cursor
						.getColumnIndex("updated_at"));
			}
		}
		cursor.moveToFirst();
		return attendee;

	}

}
