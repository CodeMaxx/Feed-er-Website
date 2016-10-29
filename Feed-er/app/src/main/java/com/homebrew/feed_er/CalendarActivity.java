package com.homebrew.feed_er;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.homebrew.feed_er.R.id.textView;

public class CalendarActivity extends AppCompatActivity {
    private Map<Date,String> impDates;
    private Map<Date, Drawable> backgroundForDateMap;
    private CaldroidFragment caldroidFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
//        CaldroidFragment caldroidFragment = new CaldroidFragment();
//        Bundle args = new Bundle();
//        Calendar cal = Calendar.getInstance();
//        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
//        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
//        caldroidFragment.setArguments(args);
//
//        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//        t.replace(R.id.calendarView, caldroidFragment);
//        t.commit();

        caldroidFragment = new CaldroidFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, caldroidFragment);
        t.commit();

        DatesListGetter datesListGetter = new DatesListGetter();
        new Thread(datesListGetter, "DatesListGetter").start();

    }

    private class DatesListGetter implements Runnable {
        public DatesListGetter() {
            Log.d("DLG", "DLG constructed");
        }

        @Override
        public void run() {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://10.42.0.29:8000";
            impDates = new HashMap<>();
            Log.d("CLG", "sending request...");
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String responseString) {
                            Log.d("CLG", "response obtained...");
                            Log.d("Response",responseString);
                            // Display the first 500 characters of the response string.
                            try{
                                final JSONObject response = new JSONObject(responseString).getJSONArray("results").getJSONObject(0);
                                JSONArray assignDeadlines = response.getJSONArray("assignmentDeadlines");
                                JSONArray examDates = response.getJSONArray("examDates");
                                backgroundForDateMap = new HashMap<>();
                                impDates.clear();
                                final Calendar c = Calendar.getInstance();
                                for (int i = 0; i < assignDeadlines.length(); i++) {
                                    JSONObject assignDeadline = assignDeadlines.getJSONObject(i);
                                    Log.d("JSON", assignDeadline.toString());
                                    Date dL = new Date(assignDeadline.getInt("year")-1900,assignDeadline.getInt("month")-1,assignDeadline.getInt("day"));
                                    c.setTimeInMillis(dL.getTime());

                                    impDates.put(c.getTime(),assignDeadline.getString("course")+"#"+assignDeadline.getString("description"));
                                    backgroundForDateMap.put(c.getTime(), new ColorDrawable(Color.YELLOW));
                                    Log.d("JSON", assignDeadline.getString("course"));
                                    Log.d("JSON", dL.toString());
                                }
                                for (int i = 0; i < examDates.length(); i++) {
                                    JSONObject examDate = examDates.getJSONObject(i);
                                    Log.d("JSON", examDate.toString());
                                    Date dL = new Date(examDate.getInt("year")-1900,examDate.getInt("month")-1,examDate.getInt("day"));

                                    c.setTimeInMillis(dL.getTime());
                                    impDates.put(c.getTime(),examDate.getString("course")+"#"+examDate.getString("description"));
                                    backgroundForDateMap.put(c.getTime(), new ColorDrawable(Color.RED));
                                    Log.d("JSON", examDate.getString("course"));
                                    Log.d("JSON", dL.toString());
                                }

                                //listeners
                                final CaldroidListener listener = new CaldroidListener() {
                                    @Override
                                    public void onSelectDate(Date date, View view) {
                                        //Log.d("SELECT DATE", date.toString());
                                        if(impDates.containsKey(date)){
                                            Log.d("IMPDATE", impDates.get(date));
                                            backgroundForDateMap.put(date, new ColorDrawable(Color.GREEN));
                                            caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
                                            caldroidFragment.refreshView();
                                        }
                                    }


                                };

                                caldroidFragment.setCaldroidListener(listener);

                                //




                                caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
                                caldroidFragment.refreshView();
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
