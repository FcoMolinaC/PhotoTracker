package com.fmc.phototracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackList extends Activity {

    private ListView tracklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_list);

        ArrayList<ListEntries> data = new ArrayList<>();

        data.add(new ListEntries("track1", "Lorem ipsum dolor sit amet"));
        data.add(new ListEntries("track2", "consectetur adipisicing elit"));
        data.add(new ListEntries("track3", "sed do eiusmod tempor incididunt"));

        tracklist = findViewById(R.id.ListView_list);
        tracklist.setAdapter(new ListAdapter(this, R.layout.track_entries, data) {
            @Override
            public void onEntries(Object entrada, View view) {
                if (entrada != null) {
                    TextView text_name = view.findViewById(R.id.text_name);
                    if (text_name != null)
                        text_name.setText(((ListEntries) entrada).get_name());

                    TextView text_details = view.findViewById(R.id.text_details);
                    if (text_details != null)
                        text_details.setText(((ListEntries) entrada).get_details());
                }
            }
        });

        tracklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ListEntries select = (ListEntries) parent.getItemAtPosition(position);

                //To-do: mostrar el track en el mapa. Pasa la info como bundle
                Intent trackmapIntent = new Intent(TrackList.this, TrackMap.class);
                startActivity(trackmapIntent);
            }
        });
    }
}
