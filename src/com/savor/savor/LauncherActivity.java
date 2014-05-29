package com.savor.savor;

import org.json.JSONArray;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class LauncherActivity extends Activity {

	// Used to save the current location of the device
	double lat;
	double lon;

	// Used to show the loading spinner
	private ProgressBar spinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		spinner = (ProgressBar) findViewById(R.id.progressBar1);
		spinner.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.launcher, menu);

		final Button foodButton = (Button) findViewById(R.id.foodButton);
		final LauncherActivity ip = this;

		foodButton.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {

				getLocation();
				System.out.println(lat);
				System.out.println(lon);
				
				// Hides the button and a loading spinning circle appears
				foodButton.setVisibility(View.INVISIBLE);
			    spinner.setVisibility(View.VISIBLE);
				
				GetResults temp = new GetResults();
				temp.search(lat, lon, ip);

			}
		});

		return true;
	}

	private void getLocation() {
		// Get the location manager
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String bestProvider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		LocationListener loc_listener = new LocationListener() {

			public void onLocationChanged(Location l) {
			}

			public void onProviderEnabled(String p) {
			}

			public void onProviderDisabled(String p) {
			}

			public void onStatusChanged(String p, int status, Bundle extras) {
			}
		};
		locationManager
				.requestLocationUpdates(bestProvider, 0, 0, loc_listener);
		location = locationManager.getLastKnownLocation(bestProvider);

		// Turns off GPS after using it
		locationManager.removeUpdates(loc_listener);
		locationManager = null;
		try {
			lat = location.getLatitude();
			lon = location.getLongitude();
		} catch (NullPointerException e) {
			lat = -1.0;
			lon = -1.0;
		}
	}

	public void resultsReady(JSONArray results) {
		Intent intent = new Intent(this, DisplayResponsesActivity.class);

		if (results != null) {
			intent.putExtra("data", results.toString());
		    spinner.setVisibility(View.INVISIBLE); // Removes the spinning circle
			startActivity(intent);
		} else {

		}
	}

}
