package com.homebrew.feed_er;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CourseListActivity extends AppCompatActivity {
    ListView mCourseList;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        mCourseList = (ListView) findViewById(R.id.courseList);
        textView = (TextView) findViewById(R.id.textView);
        if(textView== null){
            Toast t = Toast.makeText(getApplicationContext(),"NPE",Toast.LENGTH_SHORT);
            t.show();
        }
        //textView.setText("That didn't work!");
        CourseListGetter courseListGetter = new CourseListGetter();
        new Thread(courseListGetter, "CourseListGetter").start();

    }



    private class CourseListGetter implements Runnable{
        public CourseListGetter(){
            Toast t = Toast.makeText(getApplicationContext(),"Will start thread",Toast.LENGTH_SHORT);
            t.show();
            Log.d("CLG","Whatsup??");
            Log.d("CLG","Whatsup??");
            Log.w("CLG","Whatsup??");
            Log.v("CLG","Whatsup??");
            Log.i("CLG","Whatsup??");
            //new Thread(this, "CourseListGetter").start();
        }
        @Override
        public void run(){
    // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url ="http://www.randomuser.me/api";
            System.out.println("Response recorded");

    // Request a string response from the provided URL.

            //Toast t = Toast.makeText(getApplicationContext(),"Will send request",Toast.LENGTH_SHORT);
            //t.show();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            textView.setText("Response is: "+ response.substring(0,500));
                            System.out.println("Response recorded");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //textView.setText("That didn't work!");
                }
            });
    // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }

}
