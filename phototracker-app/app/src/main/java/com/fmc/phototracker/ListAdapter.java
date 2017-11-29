package com.fmc.phototracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by FMC on 29/11/2017.
 */

public abstract class ListAdapter extends BaseAdapter {

    private ArrayList<?> entries;
    private int R_layout_IdView;
    private Context context;

    public ListAdapter(Context context, int R_layout_IdView, ArrayList<?> entries) {
        super();
        this.context = context;
        this.entries = entries;
        this.R_layout_IdView = R_layout_IdView;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R_layout_IdView, null);
        }
        onEntries (entries.get(position), view);
        return view;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract void onEntries (Object entries, View view);
}
