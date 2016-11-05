package com.homebrew.feed_er;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CalendarTabFragment extends Fragment {

    public class Deadline {
        public String name;
        public Date deadline;
        public String type;
    }

    public Deadline[] assigns;
    public Deadline[] comFBs;
    public Deadline[] incomFBs;
    public List<Deadline> total = new ArrayList<>();
    private Map<Date, Deadline> impDates;
    private Map<Date, Drawable> backgroundForDateMap;
    private CaldroidFragment caldroidFragment;
    private String token;
    ListAdapter customAdapter;
    ListView deadlineListView;

    private OnFragmentInteractionListener mListener;

    public CalendarTabFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Boolean isNull = (getActivity() == null);
        Log.d("NULL", isNull.toString());


        token = getActivity().getIntent().getExtras().getString("token");
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
        caldroidFragment.setArguments(args);
        FragmentTransaction t = getActivity().getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, caldroidFragment);
        t.commit();

        /////////////////////////////// codde /////////////////
        View rootview = inflater.inflate(R.layout.fragment_calendar_tab, container, false);
        deadlineListView = (ListView) rootview.findViewById(R.id.deadlinelistview);

// get data from the table by the ListAdapter
        Deadline sample = new Deadline();
        sample.name = "Hey";
        sample.type = "type";
        sample.deadline = new Date();
        total.add(sample);
        customAdapter = new ListAdapter(getContext(), R.layout.itemlistrow, total);

        deadlineListView.setAdapter(customAdapter);
        DatesListGetter datesListGetter = new DatesListGetter();
        new Thread(datesListGetter, "DatesListGetter").start();

        ///////////////////////// code ////////////////////////////////

        return inflater.inflate(R.layout.fragment_calendar_tab, container, false);
    }

    private class DatesListGetter implements Runnable {
        public DatesListGetter() {
            Log.d("DLG", "DLG constructed");
        }

        @Override
        public void run() {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            String url = getString(R.string.api_base_url) + "dates";
            impDates = new HashMap<>();
            Log.d("CLG", "sending request...");
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String responseString) {
                            Log.d("CLG", "response obtained...");
                            Log.d("Response",responseString);
                            Log.d("TOKEN",token);

                            try{
                                final JSONObject response = new JSONObject(responseString);
                                final JSONArray assignments = response.getJSONArray("assignment");
                                JSONArray complete_feedbacks = response.getJSONArray("complete");
                                JSONArray incomplete_feedbacks = response.getJSONArray("incomplete");
                                backgroundForDateMap = new HashMap<>();
                                impDates.clear();
                                final Calendar c = Calendar.getInstance();

                                assigns = new Deadline[assignments.length()];
                                for(int i=0;i < assignments.length(); i++) {
                                    Deadline mydeadline = new Deadline();
                                    JSONObject rDetail = (JSONObject)assignments.get(i);
                                    JSONObject rFields = (JSONObject)rDetail.get("fields");
                                    mydeadline.name = rFields.getString("name");
                                    String dds[] = rFields.getString("deadline").substring(0,10).split("-");
                                    int yyyy = Integer.parseInt(dds[0]);
                                    int mm = Integer.parseInt(dds[1]);
                                    int dd = Integer.parseInt(dds[2].substring(0,2));
                                    mydeadline.deadline = new Date(yyyy-1900,mm-1,dd);
                                    c.setTimeInMillis(mydeadline.deadline.getTime());
                                    mydeadline.type = rDetail.getString("model");
                                    backgroundForDateMap.put(c.getTime(),new ColorDrawable(Color.RED));
                                    impDates.put(c.getTime(),mydeadline);
                                    Log.d("JSON","Added");
                                    Log.e("Assignment", c.toString() );
                                    assigns[i] = mydeadline;

                                }

                                comFBs = new Deadline[complete_feedbacks.length()];
                                for(int i=0;i < complete_feedbacks.length(); i++) {
                                    Deadline mydeadline = new Deadline();
                                    JSONObject rDetail = (JSONObject)complete_feedbacks.get(i);
                                    JSONObject rFields = (JSONObject)rDetail.get("fields");
                                    mydeadline.name = rFields.getString("name");
                                    String dds[] = rFields.getString("deadline").substring(0,10).split("-");
                                    int yyyy = Integer.parseInt(dds[0]);
                                    int mm = Integer.parseInt(dds[1]);
                                    int dd = Integer.parseInt(dds[2].substring(0, 2));
                                    mydeadline.deadline = new Date(yyyy - 1900, mm - 1, dd);
                                    c.setTimeInMillis(mydeadline.deadline.getTime());
                                    mydeadline.type = rDetail.getString("model");
                                    if(impDates.containsKey(c.getTime())){
                                        backgroundForDateMap.put(c.getTime(),new ColorDrawable(Color.LTGRAY));
                                    }
                                    else backgroundForDateMap.put(c.getTime(),new ColorDrawable(Color.YELLOW));
                                    impDates.put(c.getTime(),mydeadline);
                                    Log.d("JSON","Added");
                                    comFBs[i] = mydeadline;
                                }

                                incomFBs = new Deadline[incomplete_feedbacks.length()];
                                for(int i=0;i < incomplete_feedbacks.length(); i++) {
                                    Deadline mydeadline = new Deadline();
                                    JSONObject rDetail = (JSONObject)incomplete_feedbacks.get(i);
                                    JSONObject rFields = (JSONObject)rDetail.get("fields");
                                    mydeadline.name = rFields.getString("name");
                                    String dds[] = rFields.getString("deadline").substring(0,10).split("-");
                                    int yyyy = Integer.parseInt(dds[0]);
                                    int mm = Integer.parseInt(dds[1]);
                                    int dd = Integer.parseInt(dds[2].substring(0,2));
                                    mydeadline.deadline = new Date(yyyy-1900,mm-1,dd);
                                    c.setTimeInMillis(mydeadline.deadline.getTime());
                                    mydeadline.type = rDetail.getString("model");
                                    if(impDates.containsKey(c.getTime())){
                                        backgroundForDateMap.put(c.getTime(),new ColorDrawable(Color.DKGRAY));
                                    }
                                    else backgroundForDateMap.put(c.getTime(),new ColorDrawable(Color.CYAN));
                                    impDates.put(c.getTime(),mydeadline);
                                    Log.d("JSON","Added");
                                    incomFBs[i] = mydeadline;
                                }
                                //listeners
                                final CaldroidListener listener = new CaldroidListener() {
                                    @Override
                                    public void onSelectDate(Date date, View view) {
                                        //Log.d("SELECT DATE", date.toString());
                                        Log.e("date",date.toString());
                                        Log.e("date", impDates.toString());
                                        if (impDates.containsKey(date)) {
                                            List<Deadline> assignmentsList = new ArrayList<>();
                                            List<Deadline> comFBList = new ArrayList<>();
                                            List<Deadline> incomFBList = new ArrayList<>();

                                            Log.e("popup","1");
                                            Log.e("popup","1");

                                            for(int i=0; i<assigns.length; i++){

                                                if(assigns[i].deadline.equals(date)){
                                                    Log.e("deadline", assigns[i].deadline.toString());
                                                    assignmentsList.add(assigns[i]);
                                                    Log.e("popup",assigns[i].toString());
                                                }
                                            }

                                            for(int i=0; i<comFBs.length; i++){
                                                if(comFBs[i].deadline.equals(date)){
                                                    Log.e("deadline", date.toString());
                                                    comFBList.add(comFBs[i]);
                                                }
                                            }

                                            for(int i=0; i<incomFBs.length; i++){
                                                if(incomFBs[i].deadline.equals(date)){
                                                    Log.e("deadline", date.toString());
                                                    incomFBList.add(incomFBs[i]);
                                                    Log.e("popup",incomFBs[i].toString());
                                                }
                                            }

                                            total.addAll(incomFBList);
                                            total.addAll(comFBList);
                                            total.addAll(assignmentsList);
                                            customAdapter.clear();
                                            customAdapter.addAll(total);
                                            customAdapter.notifyDataSetChanged();
                                        }
                                    }


                                };

                                caldroidFragment.setCaldroidListener(listener);

                                caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
                                caldroidFragment.refreshView();
                            } catch (JSONException e) {
//                                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//                                SharedPreferences.Editor editor = sharedPref.edit();
//                                editor.remove("token");
//                                editor.remove("fullname");
//                                editor.commit();
//
//                                Intent intent = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
//                                intent.putExtra("status","multiple");
//                                startActivity(intent);
                                Log.d("Logout", "Logged Out");
                            }
                            //System.out.println("Response recorded");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //textView.setText("Please check your internet connection.");
                    Log.d("DATES", "response not received");
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", token);
                    Log.d("TOKEN", token);
                    return params;
                }

            };
            queue.add(stringRequest);
        }
    }


    // : Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    ////////////////////////////////// code //////////////////////////
    public class ListAdapter extends ArrayAdapter<Deadline> {

        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, List<Deadline> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            Log.e("getview", "getview");
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.itemlistrow, null);
            }

            Deadline p = getItem(position);

            if (p != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.id);
                TextView tt2 = (TextView) v.findViewById(R.id.categoryId);
                TextView tt3 = (TextView) v.findViewById(R.id.description);

                if (tt1 != null) {
                    tt1.setText(p.name);
                }

                if (tt2 != null) {
                    tt2.setText(p.type);
                }

                if (tt3 != null) {
                    tt3.setText(p.deadline.toString());
                }
            }

            return v;
        }

    }

    ///////////////// code /////////////////////
}
