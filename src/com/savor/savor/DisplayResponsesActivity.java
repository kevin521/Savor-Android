package com.savor.savor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class DisplayResponsesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_responses);

		Intent intent = getIntent();
		String message = intent.getStringExtra("data");

		try {
			JSONArray businesses = new JSONArray(message);

			int randNum = (int) (Math.random() * businesses.length());

			// Grab the image url from the JSONArray of the first object
			String imageUrl = businesses.getJSONObject(randNum).getString(
					"image_url");

			// Gets the better quality picture
			imageUrl = imageUrl.substring(0, imageUrl.length() - 6) + "l.jpg";

			// Uses AsyncTask below to grab the image
			Bitmap image = new GetImage().execute(imageUrl).get();

			// Sets the background a blurry image of the restaurant
			Bitmap backgroundImg = CreateBlurredImage(15, image);
			Drawable d = new BitmapDrawable(getResources(), backgroundImg);
			LinearLayout background = (LinearLayout) findViewById(R.id.rest_background);
			background.setBackground(d);

			// Sets the imageview with the returned image
			ImageView restPic = (ImageView) findViewById(R.id.rest_pic);
			restPic.setImageBitmap(image);
			restPic.setVisibility(View.VISIBLE);

			// Sets the text with the name of the restaurant
			TextView restName = (TextView) findViewById(R.id.rest_name);
			restName.setText(businesses.getJSONObject(randNum).get("name")
					.toString());
			restName.setVisibility(View.VISIBLE);
			
			// Gets the rest of the info of the restaurant and posts it
			

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_display_responses, container, false);
			return rootView;
		}
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

	// BELOW IS SUPPORT METHODS TO MAKE BITMAP BLURRY

	private Bitmap CreateBlurredImage(int radius, Bitmap b) {

		// Sets the background as the same image but blurry
		int orgWidth = b.getWidth();
		int orgHeight = b.getHeight();

		// This makes a copy of the image to be blurry
		Bitmap backgroundImg = Bitmap.createBitmap(orgWidth, orgHeight,
				Bitmap.Config.ARGB_8888);
		int[] pixels = new int[orgWidth * orgHeight];
		b.getPixels(pixels, 0, orgWidth, 0, 0, orgWidth, orgHeight);
		backgroundImg.setPixels(pixels, 0, orgWidth, 0, 0, orgWidth, orgHeight);

		// Load a clean bitmap and work from that.
		Bitmap originalBitmap = backgroundImg;

		// Create another bitmap that will hold the results of the filter.
		Bitmap blurredBitmap;
		blurredBitmap = Bitmap.createBitmap(originalBitmap);

		// Create the Renderscript instance that will do the work.
		RenderScript rs = RenderScript.create(this);

		// Allocate memory for Renderscript to work with
		Allocation input = Allocation.createFromBitmap(rs, blurredBitmap);
		// Allocation.CreateFromBitmap(rs,
		// originalBitmap,Allocation.MipmapControl.MIPMAP_FULL,
		// AllocationUsage.Script);
		Allocation output = Allocation.createTyped(rs, input.getType());

		// Load up an instance of the specific script that we want to use.
		ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs,
				Element.U8_4(rs));
		script.setInput(input);

		// Set the blur radius
		script.setRadius(radius);

		// Start the ScriptIntrinisicBlur
		script.forEach(output);

		// Copy the output to the blurred bitmap
		output.copyTo(blurredBitmap);

		return blurredBitmap;
	}

}
