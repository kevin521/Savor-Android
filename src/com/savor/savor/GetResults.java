package com.savor.savor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetResults {
	ProgressDialog pd;
	LauncherActivity ip = null;
	double lat = 40.442;
	double lon = -79.939;

	public void search(double latArg, double lonArg, LauncherActivity ip) {
		this.ip = ip;
		lat = latArg;
		lon = lonArg;
		new AsyncLocationSearch().execute();
	}

	private class AsyncLocationSearch extends
			AsyncTask<String, Void, JSONArray> {
		protected void onPreExecute() {
			pd = new ProgressDialog(ip);
			pd.setTitle("Loading...");
			pd.setMessage("Please wait.");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();
		}

		protected JSONArray doInBackground(String... urls) {
			try {
				return getList(lat, lon);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(JSONArray questions) {
			pd.dismiss();
			ip.resultsReady(questions);
		}

		private JSONArray getList(double lat, double lon) throws IOException,
				JSONException {
			JSONArray results = null;

			URL u = new URL("http://feed-me-local.appspot.com/?term=food&lat="
					+ lat + "&lon=" + lon);
			final URLConnection conn = u.openConnection();
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());

			byte[] response = new byte[0];
			String result = "";

			byte[] buff = new byte[1024];
			int k = -1;
			while ((k = conn.getInputStream().read(buff, 0, buff.length)) > -1) {

				// temp buffer size = bytes already read + bytes last read
				byte[] tbuff = new byte[response.length + k];
				System.arraycopy(response, 0, tbuff, 0, response.length);
				// copy previous bytes
				System.arraycopy(buff, 0, tbuff, response.length, k);
				// copy current lot
				response = tbuff; // call the temp buffer as your result buff
			}
			result = new String(response);

			// If there are no results, it returns an html document but if there
			// are results, it returns a json document. As such, only try to
			// make a JSONAray if there are results in JSON format.
			if (!result.contains("<html>")) {
				JSONObject temp = new JSONObject(result);
				results = temp.getJSONArray("businesses");
				System.out.println(results.getJSONObject(0).get("name"));
			}

			bis.close();

			return results;

		}
	}
}