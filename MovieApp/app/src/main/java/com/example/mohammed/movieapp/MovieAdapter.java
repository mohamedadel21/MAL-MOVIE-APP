package com.example.mohammed.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.example.mahmoud.movieapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MovieAdapter extends BaseAdapter {

    Context context;
    MovieData [] movieData;
    public MovieAdapter(Context context, MovieData [] movieData) {
        this.context = context;
        this.movieData = movieData;
    }

    @Override
    public int getCount() {
        return movieData.length;
    }

    @Override
    public Object getItem(int i) {
        return movieData[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_movie,viewGroup,false);
        }
        ImageView imgView = (ImageView) view.findViewById(R.id.list_item_movie_imageView);
        String baseUrl = "http://image.tmdb.org/t/p/w185";
        String poster_url = baseUrl+movieData[i].getPoster_url();
        Picasso.with(context).load(poster_url).into(imgView);
        return view;
    }
}
