package com.android.weathertracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteFetch {

    private static final String TAG = "RemoteFetch";
    private static final String OPEN_WEATHER_APP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";

    public static JSONObject getJSON(Activity activity, String city) {
        BufferedReader reader = null;
        try {
            URL url = new URL(String.format(OPEN_WEATHER_APP_API, city));
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.addRequestProperty("x-api-key", activity.getString(R.string.open_weather_maps_app_id));

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String temp = "";
            while ((temp = reader.readLine()) != null) {
                json.append(temp).append("\n");
            }
            reader.close();

            JSONObject resultData = new JSONObject(json.toString());

            Intent intent = new Intent(WeatherActivity.ACTION_EXTRA_HIDE_SPINNER);
            activity.sendBroadcast(intent);

            if (resultData.getInt("cod") != 200) {
                return null;
            }

            // save city if valid in shared preference
            new CityPreference(activity).setCity(city);

            return resultData;

        } catch (IOException | JSONException e) {
            if (e instanceof IOException) {
                Log.e(TAG, "Could not open URL connection", e);
            } else {
                Log.e(TAG, "Error while constructing JSON Object with weather information", e);
            }
            hideProgressBar(activity);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception", e);
            hideProgressBar(activity);
            return null;
        }
    }

    private static void hideProgressBar(Context context) {
        Intent intent = new Intent(WeatherActivity.ACTION_EXTRA_HIDE_SPINNER);
        context.sendBroadcast(intent);
    }

}
