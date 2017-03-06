package com.aashish.driverassist;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity{

    private Thread thread;
    TextView status,steerReading,rpmReading,speedReading;
    EditText tripID;
    ImageView barcode;
    Bitmap bitmap;
    Button drive;
    String drivebutton,tripidtxt;
    Snackbar snackbar;
    ConstraintLayout constraintLayout;
    CoordinatorLayout Main;
    private ProgressDialog pDialog;
    ListView list;
    String barcodetxt;
    String startpoint;
    String endpoint;
    String loadcapacity;
    String totalcapacity;
    String truckid;
    String name;
    String phone;
    String aadhar;
    String score;
    public final static int QRcodeWidth = 500;

    private List<Driver> driverList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DriverAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isOnline()) {
            setContentView(R.layout.check_connection);
        } else {
            setContentView(R.layout.activity_main);
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setTitle(getString(R.string.submit));

            getRank();
            barcode = (ImageView) findViewById(R.id.barcode);
            drive = (Button) findViewById(R.id.drive);
            steerReading = (TextView) findViewById(R.id.steer);
            status = (TextView) findViewById(R.id.status);
            rpmReading = (TextView) findViewById(R.id.rpm);
            speedReading = (TextView) findViewById(R.id.speed);
            tripID = (EditText) findViewById(R.id.tripid);
            constraintLayout = (ConstraintLayout) findViewById(R.id.main);
            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

            mAdapter = new DriverAdapter(driverList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);


            drive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tripidtxt = tripID.getText().toString();
                    if (!tripidtxt.equals("")) {
                        drivebutton = drive.getText().toString();
                        if (drivebutton.equals(getResources().getString(R.string.start_driving))) {
                            drive.setText(getResources().getString(R.string.stop_driving));
                            getData(tripidtxt);
                            feedMultiple();
                            thread.start();
                        } else if (drivebutton.equals(getResources().getString(R.string.stop_driving))) {
                            drive.setText(getResources().getString(R.string.start_driving));
                            thread.interrupt();
                        }
                    } else {
                        snackbar = Snackbar
                                .make(constraintLayout, getString(R.string.trip_id), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
            });

        }
    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }


    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                rpm();
                steer();
                speed();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!thread.isInterrupted()) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void rpm() {
        Random r = new Random();
        int rpm = r.nextInt(1500-0) + 0;
        if(rpm<800)
        {
            status.setText(getResources().getText(R.string.status_rpm_low_alert));
            status.setBackgroundColor(Color.RED);
            rpmReading.setText(String.valueOf(rpm));
        }
        else if (rpm>1000)
        {
            status.setText(getResources().getText(R.string.status_rpm_high_alert));
            status.setBackgroundColor(Color.RED);
            rpmReading.setText(String.valueOf(rpm));
        }
        else
        {
            status.setText(getResources().getText(R.string.status_eco_mode));
            status.setBackgroundColor(Color.GREEN);
            rpmReading.setText(String.valueOf(rpm));
        }
    }

    private void steer(){
        Random r = new Random();
        steerReading.setText(String.valueOf(r.nextInt(25-0) + 0));
    }

    private void speed() {
        Random r = new Random();
        speedReading.setText(String.valueOf(r.nextInt(100-0) + 0));
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(Value,BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        Color.BLACK:Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    private void getData(final String tripid) {

        // Tag used to cancel the request
        String tag_string_req = "req_trip";

        showDialog();

        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                BuildConfig.URL_trip, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        startpoint = jObj.getString("startpoint");
                        endpoint = jObj.getString("endpoint");
                        loadcapacity = jObj.getString("loadcapacity");
                        totalcapacity = jObj.getString("totalcapacity");
                        truckid = jObj.getString("truckid");
                        name = jObj.getString("name");
                        phone = jObj.getString("drivinglicense");
                        aadhar = jObj.getString("aadhar");
                        score = jObj.getString("score");

                        barcodetxt = "Startpoint :" + startpoint.toString() + "\n"
                                + "Endpoint :" + endpoint.toString() + "\n"
                                + "load Capacity :" + loadcapacity.toString() + "\n"
                                + "Total Capacity :" + totalcapacity.toString() + "\n"
                                + "Truck id :" + truckid.toString() + "\n"
                                + "Driver's Name :" + name.toString() + "\n"
                                + "Driver's Phone:" + phone.toString() + "\n"
                                + "Driver's Aadhar:" + aadhar.toString();

                        try {
                            bitmap = TextToImageEncode(barcodetxt);
                            barcode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }


                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        snackbar = Snackbar
                                .make(constraintLayout, getResources().getString(R.string.no_id), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    snackbar = Snackbar
                            .make(constraintLayout, e.toString(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                snackbar = Snackbar
                        .make(constraintLayout, error.getMessage(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("tripid", tripid);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void getRank() {

        // Tag used to cancel the request
        String tag_string_req = "req_rank";

        showDialog();

        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                BuildConfig.URL_rank, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONArray name = jObj.getJSONArray("name");
                        JSONArray score = jObj.getJSONArray("score");

                        for (int i = 0; i < name.length(); i++) {
                            Driver driver = new Driver(name.getString(i), score.getString(i));
                            driverList.add(driver);

                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        snackbar = Snackbar
                                .make(constraintLayout, getResources().getString(R.string.no_id), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    snackbar = Snackbar
                            .make(constraintLayout, e.toString(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                snackbar = Snackbar
                        .make(constraintLayout, error.getMessage(), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("a", "a");

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}

