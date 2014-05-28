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

public class LauncherActivity extends Activity {

	double lat;
	double lon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.launcher, menu);

		Button foodButton = (Button) findViewById(R.id.foodButton);
		final LauncherActivity ip = this;

		foodButton.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {

				getLocation();
				System.out.println(lat);
				System.out.println(lon);

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
			startActivity(intent);
		}
		else{
			
		}
	}

}
