package com.example.mohammed.movieapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mahmoud.movieapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DetailActivityFragment extends Fragment {

    TextView ov;
    Intent back;
    static String poster_url;
    static String title;
    static String date_;
    static String vote_;
    static String overview;
    static String id;
    static boolean land = true ;
    static FavoriteAdapter favoriteAdapter;
    private ArrayList<String> movieArrayList = null;
    TrailerAdapter trailerAdapter;
    GridView trailergGridView;
    static View view;
    static Context context;
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detail, container, false);
        context = getActivity();

        if(land==true){
            Toast.makeText(getActivity(), "Press Button", Toast.LENGTH_SHORT).show();
//            if(id != null){
//
//            }
        }else {
            back = getActivity().getIntent();
            poster_url = back.getStringExtra("poster_url");
            title = back.getStringExtra("title");
            date_ = back.getStringExtra("date");
            vote_ = back.getStringExtra("vote");
//            overview = back.getStringExtra("ov");
            id = back.getStringExtra("id");
            MovieTask task = new MovieTask();
            task.execute(id);
            MovieTask3 task2 = new MovieTask3();
            task2.execute(id);
            ImageView poster = (ImageView) view.findViewById(R.id.movie_image);
            String baseUrl = "http://image.tmdb.org/t/p/w185";
            poster_url = baseUrl+poster_url;
            Picasso.with(getActivity()).load(poster_url).into(poster);
            TextView titel = ((TextView) view.findViewById(R.id.movie_name_text));
            titel.setText(title);
            TextView date = ((TextView) view.findViewById(R.id.movie_date_text));
            date.setText(date_);
            TextView vote = ((TextView) view.findViewById(R.id.movie_averge_text));
//            vote.setText(vote_);
            ov = ((TextView) view.findViewById(R.id.movie_overview_text));
            ov.setText(overview);
            final Button favorite = (Button) view.findViewById(R.id.favorite);
            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    favBtn();
                }
            });
            trailergGridView = (GridView) view.findViewById(R.id.trailer_view);
            trailergGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movieArrayList.get(i))));
                }
            });

        }
        this.setHasOptionsMenu(true);
        return view;
    }
    public static void favBtn(){
        favoriteAdapter = new FavoriteAdapter(context);
        if (favoriteAdapter.getData(id) == null) {
            long insrt = favoriteAdapter.insertData(id, poster_url, title, date_, vote_, overview);
            if (insrt != -1) {
                Toast.makeText(context, "Favorite", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, "Error In Insert Data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Favorite Already", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.share) {
            shareNews();
        }
        return super.onOptionsItemSelected(item);
    }
    public void shareNews() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + overview);
        startActivity(Intent.createChooser(shareIntent, "Share using"));
    }
        public class MovieTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            Log.d("message", "message");
            String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/movie/"+ params[0]+"/videos?";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;


            try {

                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, "9490ec35a6eea2efe32378982073f7a3")
                        .build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.d("message", "InputStream");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.d("message", "buffer");

                    return null;
                }
                JsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("error", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("error", "Error closing stream", e);
                    }
                }
            }

            try {
                Log.d("message", "data");

                return getDataFromJson(JsonStr);
            } catch (JSONException e) {
                Log.e("error", e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            Log.d("message", "null");
            return null;
        }
        @Override
        protected void onPostExecute(String[] movie) {
            if (movie != null) {
                movieArrayList = new ArrayList<>();
                for (int i = 0; i < movie.length; i++) {
                    movieArrayList.add("https://www.youtube.com/watch?v=" + movie[i]);
                }
                trailergGridView = (GridView) view.findViewById(R.id.trailer_view);
                trailerAdapter = new TrailerAdapter(getActivity(), movieArrayList);
                trailergGridView.setAdapter(trailerAdapter);
            }
        }

        private String[] getDataFromJson(String jsonStr) throws JSONException {
            final String M_LIST = "results";
            final String key = "key";
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray(M_LIST);

            String[] results = new String[movieArray.length()];

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                String poster_key = movie.getString(key);
                results[i] = poster_key;
            }
            return results;
        }

    }

    public class MovieTask3 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            Log.d("message", "message");
            String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/movie/"+ params[0]+"/reviews?";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;


            try {

                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, "9490ec35a6eea2efe32378982073f7a3")
                        .build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.d("message", "InputStream");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.d("message", "buffer");

                    return null;
                }
                JsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("error", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("error", "Error closing stream", e);
                    }
                }
            }

            try {
                Log.d("message", "data");

                return getDataFromJson(JsonStr);
            } catch (JSONException e) {
                Log.e("error", e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            Log.d("message", "null");
            return null;
        }
        @Override
        protected void onPostExecute(String movie) {
            if (movie != null) {
                ov.setText(movie);
            }
        }

        private String getDataFromJson(String jsonStr) throws JSONException {
            final String M_LIST = "results";
            final String author = "author";
            final String content = "content";
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray(M_LIST);

            String review = null;

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                String auther = movie.getString(author);
                String contnt = movie.getString(content);
                review = auther+"\n"+contnt;
            }
            return review;
        }

    }

}
