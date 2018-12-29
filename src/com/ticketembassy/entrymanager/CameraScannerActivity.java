package com.ticketembassy.entrymanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.rockers.ticketing.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Pojo.TicketCheckPojo;
import api.ApiClient;
import api.RestInterface;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraScannerActivity extends SherlockActivity implements MessageDialogFragment.MessageDialogListener,
        ZXingScannerView.ResultHandler, FormatSelectorDialogFragment.FormatSelectorDialogListener,
        CameraSelectorDialogFragment.CameraSelectorDialogListener {
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;
    String eventid;
    private RestInterface restInterface;
    ImageView imageviewmain;
    LinearLayout linearlayoutfooter;
    TextView title;
    private Date datenow;
    private TextView success_fail_text;
    private TextView response_text;
    TextView labelno;
    TextView labelago;
    ImageView status;

    String eventtitle;
    String eventsatrtdatetime;
    private int ticket;
    private static final int REQUEST_GET_ACCOUNT = 112;
    private static final int PERMISSION_REQUEST_CODE = 200;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    ViewGroup contentFrame;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        eventid = getIntent().getStringExtra("param");
      //  eventtitle = getIntent().getStringExtra("event_title");
      //  eventsatrtdatetime = getIntent().getStringExtra("event_start_date_time");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.scanner);

        if(state != null) {
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else {
            mFlash = false;
            mAutoFocus = true;
            mSelectedIndices = null;
            mCameraId = -1;
        }

        setContentView(R.layout.activity_simple_scanner);

        eventid = getIntent().getStringExtra("param");
        imageviewmain = (ImageView) findViewById(R.id.imageView1);
        title = (TextView) findViewById(R.id.textView1);
        labelno = (TextView) findViewById(R.id.textView2);
        labelago = (TextView) findViewById(R.id.textView3);
        linearlayoutfooter = (LinearLayout) findViewById(R.id.linear_footer);
        status = (ImageView) findViewById(R.id.status);
         contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        setupFormats();
        contentFrame.addView(mScannerView);
        int currentapiVersion = Build.VERSION.SDK_INT;
              checkCameraHardware(CameraScannerActivity.this);
              getCameraInstance();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

          if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        } else {
            openCamera();
        }

    }
    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(CameraScannerActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);

                    }
                });
        alertDialog.show();
    }
    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(CameraScannerActivity.this);

                    }
                });
        alertDialog.show();
    }
    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        //i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void openCamera() {
        mScannerView = new ZXingScannerView(this);
        setupFormats();
        contentFrame.addView(mScannerView);
    }
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices);
        outState.putInt(CAMERA_ID, mCameraId);
    }



    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle presses on the action bar items
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        }else if(i == R.id.menu_flash){
            mFlash = !mFlash;
            if(mFlash){
                item.setTitle(R.string.flash_on);
            }else {
                item.setTitle(R.string.flash_off);
            }
            mScannerView.setFlash(mFlash);
        }else if(i == R.id.menu_auto_focus){
            mAutoFocus = !mAutoFocus;
            if(mAutoFocus) {
                item.setTitle(R.string.auto_focus_on);
            } else {
                item.setTitle(R.string.auto_focus_off);
            }
            mScannerView.setAutoFocus(mAutoFocus);

        }else if(i ==R.id.menu_formats){

        }else if(i == R.id.menu_camera_selector){
            mScannerView.stopCamera();
        }
        else if (i == R.id.menu_search) {
        }
        return true;
//        switch (item.getItemId()) {
//            case R.id.menu_flash:
//                mFlash = !mFlash;
//                if(mFlash) {
//                    item.setTitle(R.string.flash_on);
//                } else {
//                    item.setTitle(R.string.flash_off);
//                }
//                mScannerView.setFlash(mFlash);
//                return true;
//            case R.id.menu_auto_focus:
//                mAutoFocus = !mAutoFocus;
//                if(mAutoFocus) {
//                    item.setTitle(R.string.auto_focus_on);
//                } else {
//                    item.setTitle(R.string.auto_focus_off);
//                }
//                mScannerView.setAutoFocus(mAutoFocus);
//                return true;
//            case R.id.menu_formats:
//
//                return true;
//            case R.id.menu_camera_selector:
//                mScannerView.stopCamera();
//
//                return true;
//            case R.id.home:
//                finish();
//
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
    }



    @Override
    public void handleResult(Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        datenow = new Date();
        DatabaseHelper
                .getInstance(CameraScannerActivity.this)
                .executeDMLQuery(
                        String.format(
                                "Insert into tickets (`event_id`,`sync_status`,`ticket_no`) values(%s,1,'%s')",
                                eventid, rawResult.getText()));
        Cursor crs = DatabaseHelper.getInstance(CameraScannerActivity.this)
                .executeQuery("select max(id) as id from `tickets`");
        crs.moveToNext();
        ticket = crs.getInt(crs.getColumnIndex("id"));
        crs.moveToFirst();

        restInterface = ApiClient.getClient().create(RestInterface.class);
        Call<TicketCheckPojo> call = restInterface.TicketCheck(Constants.USER_ID,eventid,rawResult.getText());
        call.enqueue(new Callback<TicketCheckPojo>() {
            @Override
            public void onResponse(Call<TicketCheckPojo> call, Response<TicketCheckPojo> response) {
                if (response.body().isSuccess()) {
                    try {
                        imageviewmain.setVisibility(View.GONE);
                        linearlayoutfooter.setVisibility(View.VISIBLE);

                        //	labelago.setText(((JSONObject) json).getString("message"));
                        labelago.setText(response.body().getMessage());
                        //	labelno.setText(((JSONObject) json).getString("attendee_id"));

                        labelno.setText(Integer.toString(response.body().getAttendee_id()));



                        if(response.body().getStatus()==1){
                            final Dialog fbDialogue = new Dialog(CameraScannerActivity.this, android.R.style.Theme_Black_NoTitleBar);
                            fbDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
                            fbDialogue.setContentView(R.layout.alert_success_popup);
                            fbDialogue.setCancelable(true);

                            fbDialogue.show();

                            resumeCamera();

                            final Timer timer2 = new Timer();
                            timer2.schedule(new TimerTask() {
                                public void run() {
                                    //releaseCamera();
                                    fbDialogue.dismiss();

                                    timer2.cancel(); //this will cancel the timer of the system

                                }
                            }, 2000); //
                            status.setImageResource(R.drawable.true_);

                            String str = new String("");
                            String ids = new String("");

                            DatabaseHelper
                                    .getInstance(CameraScannerActivity.this)
                                    .executeDMLQuery(
                                            String.format(
                                                    "Update `tickets` set `sync_status`=%d where `id`=%d",
                                                    0, ticket));


                            if (updateifExists((response.body().getAttendee_id()))) {

                                //		Attendee attendee = fetchevent(Integer.parseInt(((JSONObject) json).getString("attendee_id")));
                                Attendee attendee = fetchevent(response.body().attendee_id);

                                str += attendee.first_name + " ";
                                ids += "#" + attendee.id + "";

                            }


//										final AlertDialog.Builder builder = new AlertDialog.Builder(CameraTestActivity.this);
//										builder.setTitle("Tickets PI");
//										builder.setMessage(str + ids);
//										builder.setCancelable(true);
//										final AlertDialog closedialog= builder.create();
//										closedialog.show();
                            //releaseCamera();



                            final Attendee attendee = fetchevent(response.body().attendee_id);
                            //	str += "(" + attendee.ticket_type + ")";
                            final String TicketNumber = String.valueOf(response.body().getAttendee_id());
                            title.setText(str.toString());
//										Thread timerthread =new Thread(){
//											public void run() {
//												try {
//													sleep(5000);
//												} catch (InterruptedException e) {
//													e.printStackTrace();
//												}finally {
//													AlertDialog alertDialog = new AlertDialog.Builder(
//															CameraTestActivity.this).create();
//													alertDialog.setTitle("Tickets PI");
//													alertDialog.setMessage(attendee.first_name + " #" + TicketNumber);
//													alertDialog.show();
//												}
//
//											}
//										};
//										timerthread.start();

                        }else if(response.body().getStatus()==0){
                            title.setText("Not Valid ticket");
                            resumeCamera();
                        }
                        else{
                            resumeCamera();
                            //	releaseCamera();
                         //   final Dialog fbDialogue = new Dialog(CameraScannerActivity.this, android.R.style.Theme_Black_NoTitleBar);
                          //  fbDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
                       //     fbDialogue.setContentView(R.layout.alert_fail_popup);
                          //  fbDialogue.setCancelable(true);

                          //  fbDialogue.show();
                            final Timer timer2 = new Timer();
                            timer2.schedule(new TimerTask() {
                                public void run() {
                                    //releaseCamera();


                                    timer2.cancel(); //this will cancel the timer of the system

                                }
                            }, 3000); //
                            status.setImageResource(R.drawable.delete);
                            //	title.setText(((JSONObject) json).getString("message"));
                            title.setText(response.body().getMessage());
                          //  fbDialogue.dismiss();
                          //  mScannerView.resumeCameraPreview(CameraScannerActivity.this);

                          //  fbDialogue.dismiss();




                        }

                        updateago();
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

      //  showMessageDialog("Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString());
    }







    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Resume the camera
        mScannerView.resumeCameraPreview(this);
    }


    @Override
    public void onFormatsSaved(ArrayList<Integer> selectedIndices) {
        mSelectedIndices = selectedIndices;
        setupFormats();
    }

    @Override
    public void onCameraSelected(int cameraId) {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }
       public void resumeCamera(){
           mScannerView.resumeCameraPreview(this);
       }
    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        if(mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<Integer>();
            for(int i = 0; i < ZXingScannerView.ALL_FORMATS.size(); i++) {
                mSelectedIndices.add(i);
            }
        }

        for(int index : mSelectedIndices) {
            formats.add(ZXingScannerView.ALL_FORMATS.get(index));
        }
        if(mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();

    }
    public Boolean updateifExists(int id) {
        if (DatabaseHelper.getInstance(CameraScannerActivity.this)
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
        Cursor cursor = DatabaseHelper.getInstance(CameraScannerActivity.this)
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

}
