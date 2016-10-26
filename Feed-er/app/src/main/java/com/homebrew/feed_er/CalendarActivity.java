package com.homebrew.feed_er;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.roomorama.caldroid.CaldroidFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.homebrew.feed_er.R.id.textView;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        CaldroidFragment caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, caldroidFragment);
        t.commit();

        DatesListGetter datesListGetter = new DatesListGetter();
        new Thread(datesListGetter, "DatesListGetter").start();
    }

    private class DatesListGetter implements Runnable {
        public DatesListGetter() {
            Log.d("DLG", "DLG constructed");
            //new Thread(this, "CourseListGetter").start();
        }

        @Override
        public void run() {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "https://beta.randomapi.com/api/f0221e9d1d6f3ddb01478db39edf1ae4";
            //System.out.println("Response recorded");

            // Request a string response from the provided URL.

            //Toast t = Toast.makeText(getApplicationContext(),"Will send request",Toast.LENGTH_SHORT);
            //t.show();

            Log.d("CLG", "sending request...");
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String responseString) {
                            Log.d("CLG", "response obtained...");
                            // Display the first 500 characters of the response string.
                            try{
                                JSONObject response = new JSONObject(responseString).getJSONArray("results").getJSONObject(0);
                                JSONArray assignDeadlines = response.getJSONArray("assignmentDeadlines");
                                JSONArray examDates = response.getJSONArray("examDates");
                                CaldroidFragment caldroidFragment = new CaldroidFragment();
                                final Map<Date, Drawable> backgroundForDateMap = new HashMap<>();
                                final Calendar c = Calendar.getInstance();
                                for (int i = 0; i < assignDeadlines.length(); i++) {
                                    JSONObject assignDeadline = assignDeadlines.getJSONObject(i);
                                    Log.d("JSON", assignDeadline.toString());
                                    Date dL = new Date(assignDeadline.getInt("year")-1900,assignDeadline.getInt("month")-1,assignDeadline.getInt("day"));

                                    c.setTimeInMillis(dL.getTime());
                                    backgroundForDateMap.put(c.getTime(), new ColorDrawable(Color.YELLOW));
                                    Log.d("JSON", assignDeadline.getString("course"));
                                    Log.d("JSON", dL.toString());
                                }
                                for (int i = 0; i < examDates.length(); i++) {
                                    JSONObject examDate = examDates.getJSONObject(i);
                                    Log.d("JSON", examDate.toString());
                                    Date dL = new Date(examDate.getInt("year")-1900,examDate.getInt("month")-1,examDate.getInt("day"));

                                    c.setTimeInMillis(dL.getTime());
                                    backgroundForDateMap.put(c.getTime(), new ColorDrawable(Color.RED));
                                    Log.d("JSON", examDate.getString("course"));
                                    Log.d("JSON", dL.toString());
                                }
                                caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
                                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                                t.replace(R.id.calendarView, caldroidFragment);
                                t.commit();
                            }
                            catch (JSONException e){
                                Log.d("JSON","JSON error");
                            }
                            //System.out.println("Response recorded");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //textView.setText("Please check your internet connection.");
                    Log.d("DLG","response not received");
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }
}
