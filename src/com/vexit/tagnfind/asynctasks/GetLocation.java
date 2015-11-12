package com.vexit.tagnfind.asynctasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.vexit.tagnfind.objects.LocationCallback;

public class GetLocation extends AsyncTask<String, Void, String> {
	Context ctx;
	Double lat;
	Double longi;
	private GetLocation lgtask = null;
	ProgressDialog ShowProgress;
	Boolean insidecancel = false;
	LocationCallback callback;

	public GetLocation(Context ctx, Double lat, Double longi,
			LocationCallback callback) {
		// TODO Auto-generated constructor stub
		this.ctx = ctx;
		this.lat = lat;
		this.longi = longi;
		this.callback = callback;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		lgtask = this;
		ShowProgress = new ProgressDialog(ctx);
		ShowProgress.setMessage("Fetching location..");
		ShowProgress.setCancelable(true);
		insidecancel = false;
		ShowProgress
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						try {
							System.out.println("Inside cancel");
							Toast.makeText(ctx, "Loading Cancelled.", 3000)
									.show();
							insidecancel = true;
							lgtask.cancel(true);
							Log.i("TASK", "Cancelled");
						} catch (Exception e) {
							// TODO: handle exception
						}

					}
				});

		ShowProgress.show();
	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		/*
		 * Geocoder gcd = new Geocoder(ctx, Locale.getDefault()); List<Address>
		 * addresses; try { addresses = gcd.getFromLocation(lat, longi, 1); if
		 * (addresses.size() > 0)
		 * System.out.println(addresses.get(0).getLocality()); // cityName =
		 * addresses.get(0).getLocality(); return
		 * addresses.get(0).getAddressLine(0) + " " +
		 * addresses.get(0).getAddressLine(1) + " " +
		 * addresses.get(0).getAddressLine(2); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

		String address = String
				.format(Locale.ENGLISH,
						"http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
								+ Locale.getDefault().getCountry(), lat, longi);
		HttpGet httpGet = new HttpGet(address);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		List<Address> retList = null;

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject = new JSONObject(stringBuilder.toString());

			retList = new ArrayList<Address>();

			if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
				JSONArray results = jsonObject.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);
					String indiStr = result.getString("formatted_address");
					Address addr = new Address(Locale.ITALY);
					addr.setAddressLine(0, indiStr);
					retList.add(addr);
				}
				return retList.get(0).getAddressLine(0);
			} else {
				System.out.println(jsonObject.getString("status").toString());
				
			}

		} catch (ClientProtocolException e) {
			Log.e(GetLocation.class.getName(),
					"Error calling Google geocode webservice.", e);
		} catch (IOException e) {
			Log.e(GetLocation.class.getName(),
					"Error calling Google geocode webservice.", e);
		} catch (JSONException e) {
			Log.e(GetLocation.class.getName(),
					"Error parsing Google geocode webservice response.", e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		callback.run(result);
		ShowProgress.dismiss();
		super.onPostExecute(result);
	}

}
