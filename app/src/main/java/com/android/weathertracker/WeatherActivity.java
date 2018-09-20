package com.android.weathertracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

public class WeatherActivity extends AppCompatActivity {

    public static final String TAG = "WeatherTracker";

    // intent filters
    public static final String ACTION_EXTRA_HIDE_SPINNER =
            "com.android.weathertracker.ACTION_HIDE_SPINNER";
    public static final String ACTION_EXTRA_SHOW_SPINNER =
            "com.android.weathertracker.ACTION_SHOW_SPINNER";

    private ProgressBar mSpinner;
    private FetchListener mProgressReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        mSpinner.setMax(10);
        mSpinner.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EXTRA_HIDE_SPINNER);
        filter.addAction(ACTION_EXTRA_SHOW_SPINNER);
        mProgressReceiver = new FetchListener();
        registerReceiver(mProgressReceiver, filter);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_city) {
            showInputDialog();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mProgressReceiver);
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change City");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(this.getString(R.string.go_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    public void changeCity(String city) {
        WeatherFragment wf = (WeatherFragment)
                getSupportFragmentManager().findFragmentById(R.id.container);

        Intent intent = new Intent(ACTION_EXTRA_SHOW_SPINNER);
        sendBroadcast(intent);

        wf.changeCity(city);
    }

    private class FetchListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase(ACTION_EXTRA_HIDE_SPINNER)) {
                Log.v(TAG, "Loading finished, hide progress spinner");
                mSpinner.setVisibility(View.GONE);
            } else if (action.equalsIgnoreCase(ACTION_EXTRA_SHOW_SPINNER)) {
                Log.v(TAG, "Retrieving weather data, show progress spinner");
                mSpinner.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "Received wrong action, unexpected case!");
            }
        }
    }
}
