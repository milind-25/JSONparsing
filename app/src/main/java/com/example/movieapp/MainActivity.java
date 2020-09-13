package com.example.movieapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    String url = "https://velmm.com/apis/volley_array.json";
    RecyclerView recyclerView;
    MoviesAdapter adaptor;
    List<Movies> movies = new ArrayList<>();
    List<Movies> moviesList = new ArrayList<>();

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        try {
            moviesList = getData(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        adaptor = new MoviesAdapter(MainActivity.this, movies);
        recyclerView.setAdapter(adaptor);
        recyclerView.setHasFixedSize(true);

    }

    private Future<List<Movies>> getData(final String requiredUrl) {
        Callable<List<Movies>> callable = new Callable<List<Movies>>() {
            @Override
            public List<Movies> call() throws Exception {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL(requiredUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    inputStream = connection.getInputStream();

                    StringBuilder stringBuilder = new StringBuilder();
                    if (inputStream != null) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String line = reader.readLine();
                        while (line != null) {
                            stringBuilder.append(line);
                            line = reader.readLine();
                        }
                    }

                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String movieName = jsonObject.getString("title");
                        String imageUrl = jsonObject.getString("image");
                        Movies movie = new Movies(movieName, imageUrl);
                        movies.add(movie);


                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return movies;

            }

        };
        return executorService.submit(callable);
    }
}