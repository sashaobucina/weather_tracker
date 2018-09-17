package weathertracker.android.weathertracker;

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

    Typeface weatherFont;

    private TextView mCityField;
    private TextView mUpdatedField;
    private TextView mDetailsField;
    private TextView mCurrTempField;
    private TextView mWeatherIcon;

    private Handler handler;

    public WeatherFragment() {
        handler = new Handler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(),
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

        mWeatherIcon.setTypeface(weatherFont);
        return rootView;
    }

    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if (json ==  null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.no_weather_found), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
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
                case 2: icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3: icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7: icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8: icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6: icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5: icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        mWeatherIcon.setText(icon);
    }

    public void changeCity(String city) {
        updateWeatherData(city);
    }

}
