package com.example.mohammed.movieapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import com.example.mahmoud.movieapp.R;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    GridView gridView;
    private ArrayList<String> movieArrayList = null;
    TrailerAdapter trailerAdapter;
    GridView trailergGridView;
    MovieAdapter movieAdapter;
    FavoriteAdapter favoriteAdapter;
    public MovieData[] movieData = null;
    boolean largeScreen = false;
    Context context;
    static TextView date_text;
    static TextView ov_text;
    static TextView vote_text;
    static TextView titel;
    static ImageView poster;
    String id;
    String title;
    String overview;
    static View fragDetail;
    static String currentState ="popular?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        context = getBaseContext();
        fragDetail =findViewById(R.id.fragment2);
        poster = (ImageView) findViewById(R.id.movie_image);
        titel = ((TextView) findViewById(R.id.movie_name_text));
        date_text = (TextView) findViewById(R.id.movie_date_text);
        vote_text = ((TextView) findViewById(R.id.movie_averge_text));
        ov_text = ((TextView) findViewById(R.id.movie_overview_text));
        gridView = (GridView) findViewById(R.id.grid_view);

        if(isNetworkAvailable()){
            executeTask();
        }
        else{
            Toast.makeText(getBaseContext(), "No Network Available", Toast.LENGTH_LONG).show();
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if(size.x>size.y  || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE ){
            View viewGroup = (View) findViewById(R.id.fragment2);
            viewGroup.setVisibility(View.INVISIBLE);
            largeScreen=true;
            DetailActivityFragment.land=true;
        }else {
            largeScreen=false;
            DetailActivityFragment.land=false;
        }
        fragmentTransaction.commit();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (largeScreen == false){
                    Intent Deatail = new Intent(context, DetailActivity.class);
                    Deatail.putExtra("poster_url", movieData[i].getPoster_url());
                    Deatail.putExtra("title", movieData[i].getTitel());
                    Deatail.putExtra("date", movieData[i].getDate());
                    Deatail.putExtra("vote", movieData[i].getVote());
                    Deatail.putExtra("ov", movieData[i].getOverview());
                    Deatail.putExtra("id", movieData[i].getId());
                    startActivity(Deatail);
                }else {
                    title =movieData[i].getTitel();
                    String date = movieData[i].getDate();
                    String vote = movieData[i].getVote();
                    overview = movieData[i].getOverview();
                    id = movieData[i].getId();
                    fragDetail.setVisibility(View.VISIBLE);
                    String baseUrl = "http://image.tmdb.org/t/p/w185";
                    String poster_url = baseUrl+movieData[i].getPoster_url();
                    DetailActivityFragment.poster_url=poster_url;
                    DetailActivityFragment.title=title;
                    DetailActivityFragment.date_=date;
                    DetailActivityFragment.overview = overview;
                    DetailActivityFragment.id = id;
                    Picasso.with(context).load(poster_url).into(poster);
                    titel.setText(title);
                    date_text.setText(date);
                    vote_text.setText(vote);
                    
//                    ov_text.setText(overview);
                    final Button favorite = (Button) findViewById(R.id.favorite);

                    favorite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DetailActivityFragment.favBtn();
                        }
                    });

//                    final DetailActivityFragment c = new DetailActivityFragment();
                    trailergGridView = (GridView) findViewById(R.id.trailer_view);
                    MovieTask2 task = new MovieTask2();
                    task.execute(id);
                    MovieTask3 task2 = new MovieTask3();
                    task2.execute(id);
                    trailergGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movieArrayList.get(i))));
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("state", currentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentState = savedInstanceState.getString("state");
        if(currentState == null){
            currentState= "popular?";
        }
        executeTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.favorite_menu) {
            Toast.makeText(context, "Favorite", Toast.LENGTH_LONG).show();
            favoriteAdapter = new FavoriteAdapter(context);
            movieData = favoriteAdapter.getAllData();
            movieAdapter = new MovieAdapter(context, movieData );
            gridView.setAdapter(movieAdapter);
            return true;
        }
        if(id==R.id.top_rated_menu){
            Toast.makeText(context, "top rated ", Toast.LENGTH_LONG).show();
            currentState = "top_rated?";
            executeTask();
            return true;
        }
        if(id==R.id.popular_menu){
            Toast.makeText(context, "Popular ", Toast.LENGTH_LONG).show();
            currentState = "popular?";
            executeTask();
            return true;
        }
        if (id == R.id.share) {
            shareNews();
        }
        return super.onOptionsItemSelected(item);
    }
    private void shareNews() {
        if (title == null) {
            Toast.makeText(getBaseContext(), "Sorry you should Choose Film", Toast.LENGTH_LONG).show();
        } else {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + overview);
            startActivity(Intent.createChooser(shareIntent, "Share using"));
        }
    }

    public void executeTask(){
        MovieTask task = new MovieTask();
        task.execute(currentState);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public class MovieTask extends AsyncTask<String, Void, MovieData[]> {


        @Override
        protected MovieData[] doInBackground(String... params) {

            Log.d("message", "message");
            String FORECAST_BASE_URL =
                    "https://api.themoviedb.org/3/movie/" + params[0];
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
        protected void onPostExecute(MovieData[] movieData) {
            getData(movieData);
        }

        private MovieData[] getDataFromJson(String jsonStr) throws JSONException {
            final String M_LIST = "results";
            final String poster_path = "poster_path";
            final String release_date = "release_date";
            final String vote_average = "vote_average";
            final String overview = "overview";
            final String original_title = "original_title";
            final String id = "id";
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray(M_LIST);
            movieData = new MovieData[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                movieData[i] = new MovieData();
                JSONObject movie = movieArray.getJSONObject(i);
                String poster = movie.getString(poster_path);
                String release = movie.getString(release_date);
                String vote = movie.getString(vote_average);
                String overvie = movie.getString(overview);
                String title = movie.getString(original_title);
                String _id = movie.getString(id);
                movieData[i].setPoster_url(poster);
                movieData[i].setDate(release);
                movieData[i].setVote(vote);
                movieData[i].setTitel(title);
                movieData[i].setOverview(overvie);
                movieData[i].setId(_id);
            }
            return movieData;
        }

        public void getData(MovieData[] moviesData) {
            if (moviesData != null) {
                movieData = moviesData;
//                    DataBase = new DatabasAdapter(getActivity());
//                    long insrt = DataBase.insertData(id, poster_url, title, date, vote, ov);
//                    if (insrt !=-1){
//                    }else Toast.makeText(getActivity(), "Error In Insert Data", Toast.LENGTH_SHORT).show();
                movieAdapter = new MovieAdapter(getBaseContext(), moviesData);
                gridView.setAdapter(movieAdapter);

            } else
                Log.d("message", "No Data");
        }
    }

    public class MovieTask2 extends AsyncTask<String, Void, String[]> {

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
                trailergGridView = (GridView) findViewById(R.id.trailer_view);
                trailerAdapter = new TrailerAdapter(context, movieArrayList);
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
                ov_text.setText(movie);
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
