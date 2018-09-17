package weathertracker.android.weathertracker;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    SharedPreferences pref;

    public CityPreference(Activity activity) {
        pref = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    /**
     * Get the preferred city.
     *
     * @return the preferred city.
     */
    String getCity() {
        return pref.getString("city", "Toronto, CAN");
    }

    /**
     * Set the preferred city.
     *
     * @param city The city to add as preferred.
     */
    void setCity(String city) {
        pref.edit().putString("city", city).apply();
    }

}
