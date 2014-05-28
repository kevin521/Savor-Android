package com.savor.savor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.text.util.Linkify;

@SuppressLint("ValidFragment")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class DisplayResponsesActivity extends Activity {

	boolean mShowingBack = false;
	String message = "";
	ArrayList<Integer> usedRestaurantPositions = new ArrayList<Integer>();
	Random r = new Random();
	JSONObject currentRestaurant = null;

	// Options for back of card
	ArrayList<String> options = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_responses);

		Intent intent = getIntent();
		message = intent.getStringExtra("data");

		// Hide the icon and sets the text white
		android.app.ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Back");
		// actionBar.setDisplayUseLogoEnabled(false);
		int actionBarTitleId = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		if (actionBarTitleId > 0) {
			TextView title = (TextView) findViewById(actionBarTitleId);
			if (title != null) {
				title.setTextColor(Color.WHITE);
			}
		}

		if (savedInstanceState == null) {

			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();

			Fragment tempFrag = new CardFrontFragment();
			fragmentTransaction.add(R.id.card, tempFrag).commit();

		}

		final Button yesButton = (Button) findViewById(R.id.yes_button);
		yesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				flipCard();
			}
		});

		final Button noButton = (Button) findViewById(R.id.no_button);
		noButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager
						.beginTransaction();
				
				Fragment tempFrag = new CardFrontFragment();
				fragmentTransaction.replace(R.id.card, tempFrag).commit();
			}
		});
	}

	private void showResults(JSONArray businesses, View view, boolean returning)
			throws JSONException, InterruptedException, ExecutionException {

		int num = 0;

		if (returning)
			num = usedRestaurantPositions
					.get(usedRestaurantPositions.size() - 1);
		else {
			num = r.nextInt(businesses.length());
			usedRestaurantPositions.add(num);
		}

		currentRestaurant = businesses.getJSONObject(num);

		// Grab the image url from the JSONArray of the first object
		String imageUrl = businesses.getJSONObject(num).getString("image_url");
		// Gets the better quality picture
		imageUrl = imageUrl.substring(0, imageUrl.length() - 6) + "l.jpg";
		// Uses AsyncTask below to grab the image
		Bitmap image = new GetImage().execute(imageUrl).get();

		// Sets the imageview with the returned image
		ImageView restPic = (ImageView) view.findViewById(R.id.rest_pic);
		restPic.setImageBitmap(image);
		restPic.setVisibility(View.VISIBLE);

		// Sets the text with the name of the restaurant
		TextView restName = (TextView) view.findViewById(R.id.rest_name);
		restName.setText(businesses.getJSONObject(num).get("name").toString());
		restName.setVisibility(View.VISIBLE);

		TextView restCategories = (TextView) view
				.findViewById(R.id.rest_categories);
		JSONArray cats = (JSONArray) businesses.getJSONObject(num).get(
				"categories");
		String categories = "";
		for (int i = 0; i < cats.length(); i++) {
			String temp = cats.getString(i);
			if (categories.length() != 0)
				categories += ", ";
			categories += temp.substring(2, temp.indexOf("\"", 2));
		}
		restCategories.setText(categories);
		restCategories.setVisibility(View.VISIBLE);

		// Converts the meters to miles and set the text of the mileage
		TextView restDistance = (TextView) view
				.findViewById(R.id.rest_distance);
		Double miles = Double.parseDouble(businesses.getJSONObject(num)
				.get("distance").toString()) * 0.00062137119;
		DecimalFormat fmt = new DecimalFormat("0.0'0'");
		restDistance.setText(fmt.format(miles) + " miles away");
		restDistance.setVisibility(View.VISIBLE);

		// Grab the image url from the JSONArray of the first object
		String starsImageUrl = businesses.getJSONObject(num).getString(
				"rating_img_url");
		// Uses AsyncTask below to grab the image
		Bitmap starsImage = new GetImage().execute(starsImageUrl).get();

		// Sets the imageview with the returned image of stars
		ImageView restStars = (ImageView) view.findViewById(R.id.rest_stars);
		restStars.setImageBitmap(starsImage);
		restStars.setVisibility(View.VISIBLE);

		// Sets the textview with the phone number
		TextView restPhone = (TextView) view.findViewById(R.id.rest_phone);
		restPhone
				.setText(businesses.getJSONObject(num).get("phone").toString());
		restPhone.setVisibility(View.VISIBLE);
		Linkify.addLinks(restPhone, Linkify.PHONE_NUMBERS);

	}

	/**
	 * A fragment representing the front of the card.
	 */
	public class CardFrontFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View result = inflater.inflate(R.layout.front_of_cards, container,
					false);

			if (savedInstanceState == null) {
				try {
					JSONArray businesses = new JSONArray(message);

					// Check to see if returning from back of card
					if (mShowingBack) {
						showResults(businesses, result, true);
						mShowingBack = false;
					} else {
						showResults(businesses, result, false);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			return result;

		}
	}

	/**
	 * A fragment representing the back of the card.
	 */
	public class CardBackFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View result = inflater.inflate(R.layout.back_of_cards, container,
					false);

			try {
				options.clear();
				options.add("Closed: "
						+ currentRestaurant.getString("is_closed"));
				options.add("Get Directions");
				options.add("Call " + currentRestaurant.getString("phone"));
				options.add("More Info");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this.getActivity(), R.layout.card_info, options);

			ListView listView = (ListView) result.findViewById(R.id.listView1);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					System.out.println("Items: " + options.get(arg2));

					// When you click the call button
					if (options.get(arg2).contains("Call")) {
						Intent call = new Intent(Intent.ACTION_DIAL);
						try {
							call.setData(Uri.parse(currentRestaurant
									.getString("phone")));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						startActivity(call);
					}

					if (options.get(arg2).contains("Get")) {
						String uri;
						try {
							uri = "geo:0,0?q="
									+ currentRestaurant.getJSONObject(
											"location").getString(
											"display_address");
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(uri));
							intent.setClassName("com.google.android.apps.maps",
									"com.google.android.maps.MapsActivity");
							try {
								startActivity(intent);
							} catch (ActivityNotFoundException ex) {
								try {
									Intent unrestrictedIntent = new Intent(
											Intent.ACTION_VIEW, Uri.parse(uri));
									startActivity(unrestrictedIntent);
								} catch (ActivityNotFoundException innerEx) {
									// Toast.makeText(this,
									// "Please install a maps application",
									// Toast.LENGTH_LONG).show();
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			});
			listView.setAdapter(adapter);

			return result;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_responses, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// if (id == R.id.action_settings) {
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	private void flipCard() {
		if (mShowingBack) {
			getFragmentManager().popBackStack();
			return;
		}

		// Flip to the back.

		mShowingBack = true;

		// Create and commit a new fragment transaction that adds the fragment
		// for the back of
		// the card, uses custom animations, and is part of the fragment
		// manager's back stack.

		getFragmentManager().beginTransaction()

		// Replace the default fragment animations with animator
		// resources
		// representing
		// rotations when switching to the back of the card, as well as
		// animator
		// resources representing rotations when flipping back to the
		// front
		// (e.g. when
		// the system Back button is pressed).
				.setCustomAnimations(R.animator.card_flip_left_in,
						R.animator.card_flip_left_out,
						R.animator.card_flip_right_in,
						R.animator.card_flip_right_out)

				// Replace any fragments currently in the container view with a
				// fragment
				// representing the next page (indicated by the just-incremented
				// currentPage
				// variable).
				.replace(R.id.card, new CardBackFragment())

				// Add this transaction to the back stack, allowing users to
				// press Back
				// to get to the front of the card.
				.addToBackStack(null)

				// Commit the transaction.
				.commit();
	}

	private class GetImage extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(
						params[0]).getContent());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}

	}

}
