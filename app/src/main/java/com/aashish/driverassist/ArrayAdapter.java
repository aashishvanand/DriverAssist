package com.aashish.driverassist;

/**
 * Created by AASHI on 3/5/2017.
 */

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.Response;


public class ArrayAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;

    public ArrayAdapter(Response.Listener<String> a, ArrayList<HashMap<String, String>> d) {
        activity = (Activity) a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.name); // title
        TextView artist = (TextView)vi.findViewById(R.id.score); // artist name

        HashMap<String, String> driver = new HashMap<String, String>();
        driver = data.get(position);

        // Setting all values in listview
        title.setText(driver.get(MainActivity.KEY_NAME));
        artist.setText(driver.get(MainActivity.KEY_SCORE));
        return vi;
    }
}