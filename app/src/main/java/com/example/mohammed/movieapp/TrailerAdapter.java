package com.example.mohammed.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.mahmoud.movieapp.R;
import java.util.ArrayList;


public class TrailerAdapter extends BaseAdapter {
    private Context context;
    ArrayList<String> data;
    TrailerAdapter(Context context, ArrayList<String> data){
        this.context = context;
        this.data = data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_trailer,viewGroup, false);
        }
        TextView trailer_txt= (TextView) view.findViewById(R.id.trailer_text);
        trailer_txt.setText("Trailer "+ (i+1));
        return view;
    }
}
