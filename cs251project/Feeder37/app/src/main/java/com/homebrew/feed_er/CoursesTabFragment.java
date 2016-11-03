package com.homebrew.feed_er;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class CoursesTabFragment extends Fragment {

    TextView output;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.output_fragment, viewGroup, false);
        output= (TextView)view.findViewById(R.id.word);
        return view;
    }
    public void display(String txt){
        output.setText(txt);
    }

}
