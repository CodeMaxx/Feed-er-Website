package com.homebrew.feed_er;

import android.content.SharedPreferences;
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

    public class RateQuestion
    {
        public String name;
        public int pk;
        public int val;
    }

    public class McqQuestion
    {
        public String name;
        public  int pk;
        public McqOptions[] options;
    }

    public class ShortQuestion
    {
        public  String name;
        public String answer;
        int pk;
    }

    public class McqOptions
    {
        String name;
        int pk;
    }

    public McqQuestion[] mcq_ques_set;
    public RateQuestion[] rate_ques_set;
    public ShortQuestion[] short_ques_set;

    public int course_id;
    public int form_id;

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
                                JSONObject ques_array = new JSONObject(response);
                                JSONArray mcq_ques = ques_array.getJSONArray("mcq_ques");
                                JSONArray rate_ques = ques_array.getJSONArray("rate_ques");
                                JSONArray short_ques = ques_array.getJSONArray("short_ques");

                                for(int i = 0; i < short_ques.length(); i++)
                                {
                                    JSONObject ques = short_ques.getJSONObject(i);
                                    ShortQuestion single = new ShortQuestion();
                                    single.name = ques.getJSONObject("fields").getString("questions");
                                    single.pk = ques.getInt("pk");
                                    short_ques_set[i] = single;
                                }

                                for(int i = 0; i < mcq_ques.length(); i++)
                                {
                                    JSONObject ques = short_ques.getJSONObject(i);
                                    McqQuestion single = new McqQuestion();
                                    single.name = ques.getJSONObject("fields").getString("questions");
                                    single.pk = ques.getInt("pk");

                                    JSONArray opt_array = ques.getJSONArray("options");

                                    single.options = new McqOptions[opt_array.length()];

                                    for(int j = 0; j < opt_array.length(); j++)
                                    {
                                        JSONObject opt = opt_array.getJSONObject(i);

                                        McqOptions single_opt = new McqOptions();
                                        single_opt.pk = opt.getInt("pk");
                                        single_opt.name = opt.getJSONObject("fields").getString("text");

                                        single.options[i] = single_opt;
                                    }
                                    mcq_ques_set[i] = single;
                                }

                                for(int i = 0; i < rate_ques.length(); i++)
                                {
                                    JSONObject ques = short_ques.getJSONObject(i);
                                    RateQuestion single = new RateQuestion();
                                    single.name = ques.getJSONObject("fields").getString("questions");
                                    single.pk = ques.getInt("pk");

                                    rate_ques_set[i] = single;
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
