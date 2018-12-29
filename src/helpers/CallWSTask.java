package helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by rockers on 16/3/17.
 */

public class CallWSTask extends AsyncTask<Object, Integer, String> {

    ProgressDialog progressDialog;
    private GetJSONListener getJSONListener;
    private String responseStr;
    private String mType;
    private int pos;
    private Context mContext;


    public CallWSTask(Context context, GetJSONListener listener) {
        this.getJSONListener = listener;
        this.mContext = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Object... urls) {

        HttpURLConnection urlConnection;


        for (Object urlObj : urls) {

            try {
                URL url = new URL(urlObj.toString());


                urlConnection = HttpUtility.sendGetRequest(url.toString());
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                   /* BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }*/
                    responseStr = HttpUtility.readSingleLineRespone();
                } else {
                    //"Failed to fetch data!";
                }

            } catch (UnknownHostException sb) {

             /*   Snackbar.make(null, "Could not load data, please Try again later!", Snackbar.LENGTH_LONG)
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        this.getJSONListener.onRemoteCallComplete(responseStr);
    }
}

