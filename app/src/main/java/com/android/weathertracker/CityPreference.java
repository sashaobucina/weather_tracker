package com.android.weathertracker;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    SharedPreferences mPref;

    public CityPreference(Activity activity) {
        mPref = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    /**
     * Get the preferred city.
     *
     * @return the preferred city.
     */
    String getCity() {
        return mPref.getString("city", "Toronto, CAN");
    }

    /**
     * Set the preferred city.
     *
     * @param city The city to add as preferred.
     */
    void setCity(String city) {
        mPref.edit().putString("city", city).apply();
    }

}
