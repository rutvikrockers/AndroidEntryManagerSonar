package helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by rockers on 16/3/17.
 */

public class PostCallWSTask extends AsyncTask<Object, Integer, String> {
private GetJSONListener getJSONListener;
private String responseStr = "";
private Context mContext;

private Handler mHandler;
private Map<String, String> params;


public PostCallWSTask(Context context, Map<String, String> params, GetJSONListener listener) {
        this.getJSONListener = listener;
        this.mContext = context;
        this.params = params;

        }

@Override
protected String doInBackground(Object... urls) {
        HttpURLConnection urlConnection;

        for (Object urlObj : urls) {

        try {
        URL url = new URL(urlObj.toString());
        urlConnection = HttpUtility.sendPostRequest(url.toString(), params);
        int statusCode = urlConnection.getResponseCode();
        if (statusCode == 200) {
        responseStr = HttpUtility.readSingleLineRespone();
        } else {
        //"Failed to fetch data!";
        }
        System.out.println(responseStr);
        } catch (UnknownHostException sb) {
               /* Snackbar.make(this.view, "Could not load data, please Try again later!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
        } catch (MalformedURLException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }


        return responseStr;
        }


@Override
protected void onPreExecute() {
        super.onPreExecute();
        }

@Override
protected void onPostExecute(String s) {
        super.onPostExecute(s);
        this.getJSONListener.onRemoteCallComplete(responseStr);
        }
        }
