package com.homebrew.feed_er;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FeedbackDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        course_id =  getIntent().getIntExtra("course_id", -1);
        pk =  getIntent().getIntExtra("pk", -1);
        token =  getIntent().getStringExtra("token");
        Log.d("Feed", ((Integer)course_id).toString());

        FeedbackGetter feedbackgetter = new FeedbackGetter();
        new Thread(feedbackgetter, "FeedbackGetter").start();

        setContentView(R.layout.activity_feedback_detail);


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
    public int pk;
    public String token;

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
                                short_ques_set = new ShortQuestion[short_ques.length()];
                                mcq_ques_set =  new McqQuestion[mcq_ques.length()];
                                rate_ques_set = new RateQuestion[rate_ques.length()];
                                Log.e("JSON", "5");
                                Log.d("JSON", mcq_ques.toString());
                                ViewGroup layout = (ViewGroup)findViewById(R.id.feedbackquestions);

                                int textsize = 16;

                                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                                EditText[] editTexts = new EditText[short_ques.length()];
                                for(int i = 0; i < short_ques.length(); i++)
                                {
                                    JSONObject ques = short_ques.getJSONObject(i);
                                    ShortQuestion single = new ShortQuestion();
                                    single.name = ques.getJSONObject("fields").getString("question");
                                    single.pk = ques.getInt("pk");
                                    short_ques_set[i] = single;
                                    if(i==0) {
                                        TextView t = new TextView(getApplicationContext());
                                        t.setLayoutParams(layoutParams);
                                        t.setTextSize(textsize);
                                        t.setText("Short questions");
                                        layout.addView(t);
                                        addDummy(layout);
                                    }
                                    TextView t = new TextView(getApplicationContext());
                                    t.setLayoutParams(layoutParams);
                                    t.setText(Integer.toString(i+1) + ". " + single.name);
                                    layout.addView(t);

                                    editTexts[i] = new EditText(getApplicationContext());
                                    editTexts[i].setLayoutParams(layoutParams);
                                    layout.addView(editTexts[i]);
                                }

                                Log.e("JSON", "6");


                                RadioGroup mcqAnswers[] = new RadioGroup[mcq_ques.length()];
                                for(int i = 0; i < mcq_ques.length(); i++)
                                {
                                    JSONObject ques = mcq_ques.getJSONObject(i);
                                    McqQuestion single = new McqQuestion();
                                    single.name = ques.getJSONObject("fields").getString("question");
                                    single.pk = ques.getInt("pk");
                                    Log.e("Options", ques.toString());
                                    JSONArray opt_array = ques.getJSONArray("options");
                                    Log.e("Options", "here");


                                    single.options = new McqOptions[opt_array.length()];
                                    Log.e("JSON", "7");
                                    for(int j = 0; j < opt_array.length(); j++)
                                    {
                                        JSONObject opt = opt_array.getJSONObject(i);

                                        McqOptions single_opt = new McqOptions();
                                        single_opt.pk = opt.getInt("pk");
                                        single_opt.name = opt.getJSONObject("fields").getString("text");

                                        single.options[i] = single_opt;
                                    }
                                    Log.e("JSON", "8");
                                    mcq_ques_set[i] = single;



                                    if(i == 0) {
                                        TextView t = new TextView(getApplicationContext());
                                        t.setLayoutParams(layoutParams);
                                        t.setTextSize(textsize);
                                        t.setText("Multiple Choice questions");
                                        layout.addView(t);
                                        addDummy(layout);
                                    }

                                    TextView t = new TextView(getApplicationContext());
                                    t.setLayoutParams(new ViewGroup.LayoutParams(layoutParams));
                                    t.setTextSize(textsize);
                                    t.setText(Integer.toString(i+1) + ". " + single.name);
                                    layout.addView(t);

                                    // Add the options
                                    RadioGroup radioGroup = new RadioGroup(getApplicationContext());
                                    for(int j = 0; j < opt_array.length(); j++) {
                                        RadioButton r = new RadioButton(getApplicationContext());
                                        r.setText(single.options[j].name);
                                        r.setLayoutParams(layoutParams);
                                        r.setId(single.options[j].pk);
                                        if(j==0) {
                                            r.setChecked(true);
                                        }
                                        radioGroup.addView(r);
                                    }
                                    mcqAnswers[i] = radioGroup;
                                    layout.addView(radioGroup);


                                }
                                Log.e("JSON", "9");


                                ////////////////////////////////////


                                RatingBar[] ratingAnswers = new RatingBar[rate_ques.length()];
                                for(int i = 0; i < rate_ques.length(); i++)
                                {
                                    JSONObject ques = rate_ques.getJSONObject(i);
                                    RateQuestion single = new RateQuestion();
                                    single.name = ques.getJSONObject("fields").getString("question");
                                    single.pk = ques.getInt("pk");
                                    rate_ques_set[i] = single;
                                    if(i == 0) {
                                        TextView t = new TextView(getApplicationContext());
                                        t.setLayoutParams(layoutParams);
                                        t.setTextSize(textsize);
                                        t.setText("Rating based questions");
                                        layout.addView(t);
                                        addDummy(layout);
                                    }

                                    TextView t = new TextView(getApplicationContext());
                                    t.setLayoutParams(layoutParams);
                                    t.setTextSize(textsize);
                                    t.setText(Integer.toString(i+1) + ". " + single.name);
                                    layout.addView(t);


                                    // Add the rating bar here
                                    RatingBar ratingBar = new RatingBar(getApplicationContext(),null);
                                    ratingBar.setLayoutParams(layoutParams);
                                    ratingBar.setNumStars(5);
                                    
                                    ratingBar.setRating(3);

                                    // give it an ID equal to the question ID
                                    ratingAnswers[i] = ratingBar;
                                    layout.addView(ratingBar);
                                 }

                                Log.d("JSON", "Hurray works");
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.e("JSON", "Bad parsing");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Feeder", "No Response");
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("course_id", Integer.toString(course_id));
                    params.put("token", token);
                    params.put("feedback_id", ((Integer)pk).toString());
//                    Log.d("TOKEN",token);
//                    Log.d("ID:",Integer.toString(course_id));
                    return params;
                }

        };

            queue.add(stringRequest);
    }

        public void addDummy(ViewGroup layout) {
            View dummy;
            dummy = new View(getApplicationContext());
            dummy.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    5
            ));
            dummy.setPadding(0,10,0,10);
            dummy.setBackgroundColor(Color.parseColor("#999999"));
            layout.addView(dummy);
        }

}}
