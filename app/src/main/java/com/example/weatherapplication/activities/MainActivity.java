package com.example.weatherapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.weatherapplication.R;
import com.example.weatherapplication.databinding.ActivityMainBinding;
import com.example.weatherapplication.network.Network;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String currentLocation;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        currentLocation = intent.getStringExtra("LATITUDE") + ","
                + intent.getStringExtra("LONGTITUDE");
//        Log.v(TAG, currentLocation);

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "current.json?key=" + Network.openWeatherAPIKey
                    + "&aqi=no&q=" + currentLocation)
                .build();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s){
                super.onPostExecute(s);
                if (s != null){

                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(s);
                        JSONObject locationObject = jsonResponse.getJSONObject("location");
                        binding.locationText.setText(locationObject.getString("name"));

                        // Get current weather data from the response and update UI
                        JSONObject currentObject = jsonResponse.getJSONObject("current");
                        String temperature = currentObject.getString("temp_c");
                        binding.currentText.setText(temperature + "°C");

                        // Get forecast data from the response and update UI
                        JSONObject forecastObject = jsonResponse.getJSONObject("forecast");
                        JSONArray forecastdayObject = forecastObject.getJSONArray("forecastday");
                        JSONObject dayObject = forecastdayObject.getJSONObject(0).getJSONObject("day");
                        String minTemp = String.valueOf(Math.round(Double.parseDouble(dayObject.getString("mintemp_c"))));
                        String maxTemp = String.valueOf(Math.round(Double.parseDouble(dayObject.getString("maxtemp_c"))));
                        binding.minimumText.setText("min: " + minTemp + "°C");
                        binding.maximumText.setText("max: " + maxTemp + "°C");

                        // Get weather icon URL from the response and use Picasso library to load the image and update UI
                        JSONObject conditionObject = currentObject.getJSONObject("condition");
                        String image_url =  "https:" + conditionObject.getString("icon");
                        Picasso.get().load(image_url).resize(100, 100).into(binding.weatherImage);
                    } catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        asyncTask.execute();
    }
}