package com.homebrew.feed_er;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class FeedbackDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        course_id =  getIntent().getIntExtra("course_id", -1);
        Log.d("Feed", ((Integer)course_id).toString());
        setContentView(R.layout.activity_feedback_detail);

        //--- text view---
        TextView txtView = (TextView) findViewById(R.id.text_id);
    }

    public class Question
    {
        public String type;
        public String name;
        public int course_id;
        public int f_id;
    }

    public Question[] ques_set;
    public int course_id;

    public class FeedbackGetter implements Runnable {

        @Override
        public void run() {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = getString(R.string.api_base_url) + "course_feedback_detail";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try
                            {
                                JSONArray ques_array = new JSONArray(response);
                                ques_set = new Question[ques_array.length()];
                                for(int i = 0; i < ques_array.length(); i++)
                                {
                                    JSONObject ques = ques_array.getJSONObject(i).getJSONObject("fields");
                                    Question single = new Question();
                                    single.course_id = course_id;
                                    single.name = ques.getString("question");
                                    single.f_id = ques.getInt("feedback");
                                    single.type = ques.getString("q_type");

                                    ques_set[i] = single;
                                }

                            }
                            catch (Exception e)
                            {

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {

            };
        }
    }

}
