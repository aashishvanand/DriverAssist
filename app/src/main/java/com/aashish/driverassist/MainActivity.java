package com.aashish.driverassist;

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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

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
    ArrayAdapter adapter;
    public final static int QRcodeWidth = 500 ;
    static final String KEY_NAME = "name";
    static final String KEY_SCORE = "score";
    ArrayList<HashMap<String, String>> RankingList = new ArrayList<HashMap<String, String>>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        barcode = (ImageView) findViewById(R.id.barcode);
        drive = (Button) findViewById(R.id.drive);
        steerReading = (TextView) findViewById(R.id.steer);
        status = (TextView) findViewById(R.id.status);
        rpmReading = (TextView) findViewById(R.id.rpm);
        speedReading = (TextView) findViewById(R.id.speed);
        tripID = (EditText) findViewById(R.id.tripid);
        constraintLayout = (ConstraintLayout) findViewById(R.id.main);

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setTitle(getString(R.string.submit));


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
                        Log.d("stopping", "stopping");
                        thread.interrupt();
                    }
                }
                else
                {
                    snackbar = Snackbar
                            .make(constraintLayout, getString(R.string.trip_id), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });

        try {
            bitmap = TextToImageEncode("Vehicle id : 1001");
            barcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

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
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
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
        String tag_string_req = "req_login";

        showDialog();

        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                BuildConfig.URL_login, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONObject startpoint = jObj.getJSONObject("startpoint");
                        JSONObject endpoint = jObj.getJSONObject("endpoint");
                        JSONObject loadcapacity = jObj.getJSONObject("loadcapacity");
                        JSONObject totalcapacity = jObj.getJSONObject("totalcapacity");
                        JSONObject truckid = jObj.getJSONObject("truckid");

                        //should work
                            HashMap<String, String> map = new HashMap<String, String>();
                            // adding each child node to HashMap key => value
                            map.put(KEY_NAME, "Aravind");
                            map.put(KEY_SCORE, "90");

                            // adding HashList to ArrayList
                            RankingList.add(map);

                        list=(ListView)findViewById(R.id.list);

                        // Getting adapter by passing xml data ArrayList
                        adapter=new ArrayAdapter(this, RankingList);
                        list.setAdapter(adapter);


                        // Click event for single list row
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {


                            }
                        });

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        snackbar = Snackbar
                                .make(constraintLayout, errorMsg, Snackbar.LENGTH_SHORT);
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


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}

