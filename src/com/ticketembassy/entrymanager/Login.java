package com.ticketembassy.entrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.rockers.ticketing.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import Pojo.ResponseUserLogin;
import api.ApiClient;
import api.RestInterface;
import helpers.CallWSTask;
import helpers.GetJSONListener;
import helpers.PostCallWSTask;
import helpers.ResponseParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends SherlockActivity  {
	Map<String, String> params;
	private EditText email;
	private EditText password;
	//private LoginAsync loginaync;
	private ProgressDialog loading;
	private CallWSTask callWSTask;
	private PostCallWSTask callWSTasks;
	private RestInterface restInterface;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		setContentView(R.layout.activity_login);
		getSupportActionBar().setCustomView(R.layout.logo);
		email = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		((Button) findViewById(R.id.button_login))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						buttonClick();
					}
				});

		Cursor sql = DatabaseHelper.getInstance(this).executeQuery("select * from `user`;");
		sql.moveToFirst();
		while (sql.isAfterLast() == false) {
			String title = sql.getString(1);
			Log.e("testset", title);
			sql.moveToNext();
		}
		Log.e("testset",sql.getColumnCount()+"");
	}
	private void buttonClick() {
		params = new HashMap<String, String>();
		Boolean isvalid = true;
		EditText temp = null;
		if (TextUtils.isEmpty(email.getText().toString())) {
			email.setError(getResources().getText(R.string.required));
			isvalid = false;
			temp = email;
		}
		if (!emailValidator(email.getText().toString())) {
			email.setError(getResources().getText(R.string.invalid_email));
			isvalid = false;
			temp = email;

		}
		if (TextUtils.isEmpty(password.getText().toString())) {
			password.setError(getResources().getText(R.string.required));
			isvalid = false;
			temp = email;
		}

		if (isvalid) {
			String url = Constants.BASE_URLs+"mobile_logins/post_login";
			params.put("user_email",email.getText().toString());
			params.put("user_password",password.getText().toString());
			restInterface = ApiClient.getClient().create(RestInterface.class);
			Call<ResponseUserLogin> call = restInterface.LoginUsers(email.getText().toString(), password.getText().toString());
			call.enqueue(new Callback<ResponseUserLogin>() {
				@Override
				public void onResponse(Call<ResponseUserLogin> call, Response<ResponseUserLogin> response) {
					if(response.body().getStatus()==1){

						DatabaseHelper
								.getInstance(Login.this)
								.executeDMLQuery(String.format("Insert into `user` (`email`,`user_id`) values ('%s',%s)",
										email.getText().toString(),

										response.body().getMsg()));
						Constants.USER_ID = response
								.body().getMsg();
						Intent dashIntent = new Intent(Login.this,
								Dashboard.class);
					//	dashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(dashIntent);
						finish();
					}else{
						new AlertDialog.Builder(Login.this)
								.setTitle("")
								.setMessage(response.body().getMsg())
								.setPositiveButton("Ok",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
											}
										}).show();
					}
				}

				@Override
				public void onFailure(Call<ResponseUserLogin> call, Throwable t) {
                    t.printStackTrace();
				}
			});
		/*	callWSTasks = new PostCallWSTask(Login.this, params, new GetJSONListener() {
				@Override
				public void onRemoteCallComplete(String jsonFromWSCall) {
					if(loading!=null)
						loading.cancel();

					JSONObject response = null;
					try {
						ResponseUserLogin loginDetails = ResponseParser.parseLoginJson(jsonFromWSCall);
						response = new JSONObject(String.valueOf(loginDetails));
						if (!response.getBoolean("success"))
							if (response.getString("status").equals("1")) {
								DatabaseHelper
										.getInstance(Login.this)
										.executeDMLQuery(String.format("Insert into `user` (`email`,`user_id`) values ('%s',%s)",
												email.getText().toString(),

												response.getString("msg")));
								Constants.USER_ID = response
										.getString("msg");
								Intent dashIntent = new Intent(getApplicationContext(),
										Dashboard.class);
								dashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(dashIntent);
								finish();
							}else{
								new AlertDialog.Builder(Login.this)
										.setTitle("")
										.setMessage(response.getString("msg"))
										.setPositiveButton("Ok",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.dismiss();
													}
												}).show();
							}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});*/
			//callWSTasks.execute(url);
		//	callWSTask = new CallWSTask(this, this);
		//	callWSTask.execute(url);
		} else {
			temp.setFocusable(true);
		}
		loading = new ProgressDialog(Login.this);
		loading.setMessage(getResources().getString(R.string.login_loading));
		loading.setIndeterminate(true);
		loading.setCancelable(true);
		loading.show();



	}

	public boolean emailValidator(String email) {
		Pattern pattern;
		Matcher matcher;
		final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/*@Override
	public void onRemoteCallComplete(String jsonFromWSCall) {
		if(loading!=null)
			loading.cancel();
		JSONObject response = null;
		try {

			response = new JSONObject(jsonFromWSCall);
			if (!response.getBoolean("success"))
			if (response.getString("status").equals("1")) {
				DatabaseHelper
						.getInstance(Login.this)
						.executeDMLQuery(String.format("Insert into `user` (`email`,`user_id`) values ('%s',%s)",
								email.getText().toString(),

								response.getString("msg")));
				Constants.USER_ID = response
						.getString("msg");
				Intent dashIntent = new Intent(getApplicationContext(),
						Dashboard.class);
				dashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(dashIntent);
				finish();
			}else{
				new AlertDialog.Builder(Login.this)
						.setTitle("")
						.setMessage(response.getString("msg"))
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}


	}*/
		private class LoginAsync extends AsyncTask<Void, Void, Object> {

		@Override
		protected void onPreExecute() {
			loading = new ProgressDialog(Login.this);
			loading.setMessage(getResources().getString(R.string.login_loading));
			loading.setIndeterminate(true);
			loading.setCancelable(true);
			loading.show();

		}

		@Override
		protected Object doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Constants.BASE_URLs
					+ "mobile_logins/post_login");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("user_email", email
						.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("user_password",
						password.getText().toString()));
				
				nameValuePairs.add(new BasicNameValuePair("imei", Constants.IMEI));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				String _response = EntityUtils.toString(response.getEntity());
				Log.e("test",_response);
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
				try {
					if (((JSONObject) json).getString("status").equals("1")) {
						Log.e("test",String.format("Insert into `user` (`email`,`user_id`) values ('%s',%s)",
								email.getText().toString(),
								((JSONObject) json)
										.getString("msg")));
						DatabaseHelper
								.getInstance(Login.this)
								.executeDMLQuery(String.format("Insert into `user` (`email`,`user_id`) values ('%s',%s)",
												email.getText().toString(),
												((JSONObject) json)
														.getString("msg")));
						Constants.USER_ID = ((JSONObject) json)
								.getString("msg");
						Intent dashIntent = new Intent(getApplicationContext(),
								Dashboard.class);
						dashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(dashIntent);
						finish();
					} else {
						new AlertDialog.Builder(Login.this)
								.setTitle("")
								.setMessage(((JSONObject) json).getString("msg"))
								.setPositiveButton("Ok",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
											}
										}).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				ExecptionHandler.register(Login.this, (Exception) json);
			}
		}
	}
}
