package com.homebrew.feed_er;

import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CourseFeedbackList extends AppCompatActivity {

    public class Feedback {
        public String name;
        public int pk;
        public Date deadline;

        public Feedback() {
            name = ""; pk = -1;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    String token;
    int course_id;
    Feedback[] feedbacks;
    ArrayAdapter<Feedback> adapter;

    // Feedback list thread
    public class FeedbackThread implements Runnable {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getString(R.string.api_base_url) + "course_feedback_list";

        @Override
        public void run() {
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("RESPONSE",response);
                            // Make the json object
                            try {
                                JSONArray json_obj = new JSONArray(response);
                                feedbacks = new Feedback[json_obj.length()];
                                for(int i=0;i<json_obj.length();i++) {
                                    JSONObject blob = (JSONObject) json_obj.get(i);
                                    JSONObject fields = (JSONObject) blob.get("fields");
                                    feedbacks[i] = new Feedback();
                                    feedbacks[i].name = fields.getString("name");
                                    feedbacks[i].pk = blob.getInt("pk");
                                    String unParsedDate[] = fields.getString("deadline").split("-");
                                    int yyyy = Integer.parseInt(unParsedDate[0]), mm = Integer.parseInt(unParsedDate[1]), dd = Integer.parseInt(unParsedDate[2].substring(0,2));
                                    feedbacks[i].deadline = new Date(yyyy-1900,mm-1,dd);

                                }

                                createFeedbackView();

                            }
                            catch(Exception e) {
                                Log.e("ERROR:","ERROR in json response");
                            }
                            // Display the first 500 characters of the response string.
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR:","Error connecting.");

                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("token",token);
                    params.put("course_id",Integer.toString(course_id));
                    Log.d("TOKEN",token);
                    Log.d("ID:",Integer.toString(course_id));
                    return params;
                }

            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_feedback_list);

        Intent intent = getIntent();
        course_id = getIntent().getExtras().getInt("pk");
        token = getIntent().getExtras().getString("token");
        Log.d("TOKEN",token);
        Log.d("COURSE_ID",Integer.toString(course_id));
        FeedbackThread fThread = new FeedbackThread();
        new Thread(fThread,"fThread").start();

    }


    public void createFeedbackView() {
        ListView listView = (ListView)findViewById(R.id.CourseFeedbackView);
        adapter = new ArrayAdapter<Feedback>(getApplicationContext(),R.layout.textviewxml,feedbacks);
        listView.setAdapter(adapter);
    }
}
