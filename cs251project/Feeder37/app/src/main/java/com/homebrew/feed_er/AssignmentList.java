package com.homebrew.feed_er;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class AssignmentList extends Fragment {

    String token;
    int course_id;
    // Assignment class
    public class Assignment {
        public String name;
        public int pk;
        public Date deadline;
        public String description;

        @Override
        public String toString() { return name; }
    }

    Assignment[] assignments;
    ArrayAdapter<Assignment> assignmentadapter;

    // Feedback list thread
    public class AssignmentThread implements Runnable {

        @Override
        public void run() {
            // Request a string response from the provided URL.
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            String url = getString(R.string.api_base_url) + "course_deadlines";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
//                            Log.d("RESPONSE",response);
                            // Make the json object
                            try {
                                JSONArray json_obj = new JSONArray(response);
                                JSONArray assignmentJSON = (JSONArray) json_obj.get(1);

                                assignments = new Assignment[assignmentJSON.length()];
                                for(int i=0;i < assignmentJSON.length();i++) {
                                    JSONObject blob = (JSONObject) assignmentJSON.get(i);
                                    JSONObject fields = (JSONObject) blob.get("fields");
                                    assignments[i] = new Assignment();
                                    assignments[i].name = fields.getString("name");
                                    assignments[i].description = fields.getString("description");
                                    assignments[i].pk = blob.getInt("pk");
                                    String unParsedDate[] = fields.getString("deadline").split("-");
                                    int yyyy = Integer.parseInt(unParsedDate[0]), mm = Integer.parseInt(unParsedDate[1]), dd = Integer.parseInt(unParsedDate[2].substring(0,2));
                                    assignments[i].deadline = new Date(yyyy-1900,mm-1,dd);
                                }
                                createAssignmentView();
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                                Log.e("ERROR:","ERROR in json response assignment");
                            }
                            // Display the first 500 characters of the response string.
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR:","Error connecting assignment.");

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

    Context context;
    View view;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        token = getActivity().getIntent().getExtras().getString("token");
        course_id = getActivity().getIntent().getExtras().getInt("pk");

        Log.d("TOKEN",token);
        Log.d("COURSE_ID",Integer.toString(course_id));

        context = getActivity().getApplicationContext();
        view = inflater.inflate(R.layout.fragment_assignment_list, container, false);
        listView = (ListView)view.findViewById(R.id.AssignmentListView);

        AssignmentThread a = new AssignmentThread();
        new Thread(a,"AssignmentThread").start();

//        Log.d("Assignment thread","YES");

        // Inflate the layout for this fragment
        return view;
    }

    public void createAssignmentView() {
        assignmentadapter = new ArrayAdapter<Assignment>(getActivity().getApplicationContext(),R.layout.textviewxml,assignments);
        listView.setAdapter(assignmentadapter);
    }
}
