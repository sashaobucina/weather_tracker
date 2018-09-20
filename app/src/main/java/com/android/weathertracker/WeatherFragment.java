package com.android.weathertracker;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


public class WeatherFragment extends Fragment {
    public static final String TAG = WeatherActivity.TAG;

    /* weather icon ids */
    private static final int THUNDER_ID = 2;
    private static final int DRIZZLE_ID = 3;
    private static final int RAINY_ID = 5;
    private static final int SNOWY_ID = 6;
    private static final int FOGGY_ID = 7;
    private static final int CLOUDY_ID = 8;

    private Typeface mWeatherFont;

    /* fields containing relevant weather data */
    private TextView mCityField;
    private TextView mUpdatedField;
    private TextView mDetailsField;
    private TextView mCurrTempField;
    private TextView mWeatherIcon;

    private Handler mHandler;

    public WeatherFragment() {
        mHandler = new Handler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeatherFont = Typeface.createFromAsset(getActivity().getAssets(),
                String.format(Locale.US, "fonts/%s", "weather.ttf"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        updateWeatherData(new CityPreference(getActivity()).getCity());

        mCityField = (TextView) rootView.findViewById(R.id.city_field);
        mUpdatedField = (TextView) rootView.findViewById(R.id.updated_field);
        mDetailsField = (TextView) rootView.findViewById(R.id.details_field);
        mCurrTempField = (TextView) rootView.findViewById(R.id.current_temp_field);
        mWeatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);

        mWeatherIcon.setTypeface(mWeatherFont);
        return rootView;
    }

    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if (json ==  null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.no_weather_found), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }


    @SuppressLint("SetTextI18n")
    public void renderWeather(JSONObject json) {
        try {
            mCityField.setText(json.getString("name").toUpperCase(Locale.CANADA)
                + ", " +
                json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            mDetailsField.setText(
                    details.getString("description").toUpperCase(Locale.CANADA) +
                    "\n" + "Humidity: " + main.getString("humidity") + "%" +
                    "\n" + "Pressure: " + main.getString("pressure") + "%" + "hPa");

            mCurrTempField.setText(
                    String.format("%.2f", main.getDouble("temp")) + " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            mUpdatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (Exception e) {
            Log.e(TAG, "One or more fields not found in the JSON data", e);
        }
    }


    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currTime = new Date().getTime();
            if (currTime >= sunrise && currTime < sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case THUNDER_ID: icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case DRIZZLE_ID: icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case FOGGY_ID: icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case CLOUDY_ID: icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case SNOWY_ID: icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case RAINY_ID: icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        mWeatherIcon.setText(icon);
    }

    public void changeCity(String city) {
        updateWeatherData(city);
    }

}
