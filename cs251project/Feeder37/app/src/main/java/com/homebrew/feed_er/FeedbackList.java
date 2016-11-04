package com.homebrew.feed_er;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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


public class FeedbackList extends Fragment {

    // Feedback class
    public class Feedback {
        public String name;
        public int pk;
        public Date deadline;

        public Feedback() {
            name = "";
            pk = -1;
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


    public class FeedbackThread implements Runnable {

        @Override
        public void run() {
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            String url = getString(R.string.api_base_url) + "course_deadlines";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Make the json object
                            try {
                                JSONArray json_obj = new JSONArray(response);
                                JSONArray feedbackJSON = (JSONArray) json_obj.get(0);

                                feedbacks = new Feedback[feedbackJSON.length()];
                                for (int i = 0; i < feedbackJSON.length(); i++) {
                                    JSONObject blob = (JSONObject) feedbackJSON.get(i);
                                    JSONObject fields = (JSONObject) blob.get("fields");
                                    feedbacks[i] = new Feedback();
                                    feedbacks[i].name = fields.getString("name");
                                    feedbacks[i].pk = blob.getInt("pk");
                                    String unParsedDate[] = fields.getString("deadline").split("-");
                                    int yyyy = Integer.parseInt(unParsedDate[0]), mm = Integer.parseInt(unParsedDate[1]), dd = Integer.parseInt(unParsedDate[2].substring(0, 2));
                                    feedbacks[i].deadline = new Date(yyyy - 1900, mm - 1, dd);
                                }

                                createFeedbackView();

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("ERROR:", "ERROR in json response feedback");
                            }
                            // Display the first 500 characters of the response string.
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR:", "Error connecting feedback.");

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", token);
                    params.put("course_id", Integer.toString(course_id));
//                    Log.d("TOKEN",token);
//                    Log.d("ID:",Integer.toString(course_id));
                    return params;
                }

            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }

    Context context;
    View view;
    ListView listView;

    // make the adapter here
    public class FeedbackAdapter extends ArrayAdapter<Feedback> {


        public FeedbackAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public FeedbackAdapter(Context context, int resource, Feedback[] items) {
            super(context, resource, items);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.textviewxml, null);
            }

            final Feedback p = getItem(position);

            if (p != null) {
                TextView tt = (TextView) v.findViewById(R.id.courseTextView);
                tt.setText(p.toString());

                if (!p.name.equals("")) {
                    tt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), FeedbackDetail.class);
                            intent.putExtra("token", token);
                            intent.putExtra("pk", p.pk);
                            startActivity(intent);
                        }
                    });
                }

            }

            return v;
        }
    }

    // Create view here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getActivity().getApplicationContext();
        view = inflater.inflate(R.layout.fragment_feedback_list, container, false);
        listView = (ListView) view.findViewById(R.id.FeedbackListView);

        token = getActivity().getIntent().getExtras().getString("token");
        course_id = getActivity().getIntent().getExtras().getInt("pk");

        FeedbackThread f = new FeedbackThread();
        new Thread(f, "FeedbackThread").start();
        return view;
    }

    public void createFeedbackView() {
        adapter = new ArrayAdapter<Feedback>(getActivity().getApplicationContext(), R.layout.textviewxml, feedbacks);
        listView.setAdapter(adapter);
    }

}
