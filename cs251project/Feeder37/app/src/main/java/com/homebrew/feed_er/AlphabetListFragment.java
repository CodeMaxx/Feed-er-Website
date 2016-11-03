package com.homebrew.feed_er;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AlphabetListFragment extends ListFragment {
    String[] alphabet = new String[] { "A","B","C"};
    String[] word = new String[]{"Apple", "Boat", "Cat"};
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup viewGroup, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.menu_fragment, viewGroup, false);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_activated_1, alphabet);
        setListAdapter(adapter);
        return view;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        CoursesTabFragment txt = (CoursesTabFragment) getFragmentManager().findFragmentById(R.id.output);
        txt.display(word[position]);
        getListView().setSelector(android.R.color.holo_red_dark);
    }
}
