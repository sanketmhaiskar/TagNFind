package com.vexit.tagnfind.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vexit.tagnfind.R;
import com.vexit.tagnfind.asynctasks.GetLocation;
import com.vexit.tagnfind.databases.DatabaseHelper;
import com.vexit.tagnfind.objects.CheckNetwork;
import com.vexit.tagnfind.objects.LocationCallback;

public class ViewTagActivity extends Activity {
	protected static final String currentLatitude = null;
	protected static final String currentLongitude = null;
	protected static String fixedLatitude = null;
	protected static String fixedLongitude = null;
	Bundle mBundle;
	EditText txt_show_TAG;
	TextView txtDescription;
	LocationManager locationManager = null;
	LocationListener locationListener = null;
	static Context mContext;
	static SQLiteDatabase db = null;
	private int MAX_TIMES_WAIT = 25;
	ProgressDialog GpsFixWait;
	static int checkThreshhold = 0;
	Listener gpsListener;
	GetLocation gtlocation;
	ContentValues cv;
	Boolean flag;
	String longitude = null, latitude = null;
	DatabaseHelper dbhelper;
	RelativeLayout rel_layout_show_tag_btn, rel_layout_show_find_btn,
			rellayout_back_btn;
	Button btn_show_OK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_show);
		mContext = this;
		locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		init();

		dbhelper = new DatabaseHelper(this);
		db = dbhelper.getWritableDatabase();
		db.setLockingEnabled(true);
	}

	private void init() {
		// TODO Auto-generated method stub
		mBundle = getIntent().getExtras();
		btn_show_OK = (Button) findViewById(R.id.btn_show_OK);
		txt_show_TAG = (EditText) findViewById(R.id.txt_show_TAG);
		rel_layout_show_tag_btn = (RelativeLayout) findViewById(R.id.rel_layout_show_tag_btn);
		rel_layout_show_find_btn = (RelativeLayout) findViewById(R.id.rel_layout_show_find_btn);
		rellayout_back_btn = (RelativeLayout) findViewById(R.id.rellayout_back_btn);
		txtDescription = (TextView) findViewById(R.id.txtDescription);
		txt_show_TAG.setText(mBundle.getString("TagName"));
		txtDescription.setText(mBundle.getString("Address"));
		fixedLatitude = mBundle.getString("Latitude");
		fixedLongitude = mBundle.getString("Longitude");
		rel_layout_show_tag_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							// Yes button clicked
							if (CheckNetwork.isNetworkAvailable(mContext)) {
								flag = displayGpsStatus();

								if (flag) {

									GpsFixWait = new ProgressDialog(mContext);
									GpsFixWait
											.setMessage("Waiting for location...");
									GpsFixWait.setCancelable(true);
									GpsFixWait
											.setOnCancelListener(new DialogInterface.OnCancelListener() {

												@Override
												public void onCancel(
														DialogInterface dialog) {
													// TODO Auto-generated
													// method
													// stub
													System.out
															.println("CANCELLED");
													locationManager
															.removeGpsStatusListener(gpsListener);
													locationManager
															.removeUpdates(locationListener);

												}
											});
									GpsFixWait.show();

									System.out.println("HERE`");

									locationListener = new MyLocationListener();
									System.out.println("HERE1");
									checkThreshhold = 0;
									locationManager.requestLocationUpdates(
											LocationManager.GPS_PROVIDER, 500,
											1, locationListener);

									System.out.println("HERE3");
									gpsListener = new Listener() {

										@Override
										public void onGpsStatusChanged(int event) {
											// TODO Auto-generated method stub
											switch (event) {
											case GpsStatus.GPS_EVENT_STARTED:
												System.out
														.println("MainActivity.onCreate(...).new Listener() {...}.onGpsStatusChanged()"
																+ "1");
												break;
											case GpsStatus.GPS_EVENT_FIRST_FIX:
												System.out
														.println("MainActivity.onCreate(...).new Listener() {...}.onGpsStatusChanged()"
																+ "2");
												// Fix received ,remove
												// gpslistener
												locationManager
														.removeGpsStatusListener(gpsListener);
												break;
											case GpsStatus.GPS_EVENT_STOPPED:
												System.out
														.println("MainActivity.onCreate(...).new Listener() {...}.onGpsStatusChanged()"
																+ "3");
												locationManager
														.removeUpdates(locationListener);
												locationManager
														.removeGpsStatusListener(gpsListener);
												break;
											case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
												System.out.println("CheckNO:"
														+ checkThreshhold
														+ "::" + "4");
												if (checkThreshhold > MAX_TIMES_WAIT) {
													// Unable to get a fix so
													// use
													// Network
													// Provider location
													locationManager
															.removeUpdates(locationListener);
													locationManager
															.removeGpsStatusListener(gpsListener);
													GpsFixWait.dismiss();
													Toast.makeText(
															mContext,
															"Your location is currently unavailable. Please wait for GPS to connect and then try again.",
															3000).show();
													/*
													 * locationManager
													 * .requestLocationUpdates(
													 * LocationManager
													 * .NETWORK_PROVIDER, 0, 0,
													 * locationListener);
													 */
												}
												break;
											default:
												break;
											}
											checkThreshhold++;
										}
									};
									locationManager
											.addGpsStatusListener(gpsListener);

								} else {
									Toast.makeText(getApplicationContext(),
											"Please turn the GPS ON", 5000)
											.show();
								}
							} else {
								Toast.makeText(
										getApplicationContext(),
										"No internet connection. Please connect to internet to use GPS.",
										5000).show();
							}
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							// No button clicked
							dialog.dismiss();
							break;
						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Update tagged location ?")
						.setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener).show();

			}
		});
		rel_layout_show_find_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String uri = "http://maps.google.com/maps?&daddr="
						+ fixedLatitude + "," + fixedLongitude;
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse(uri));
				intent.setClassName("com.google.android.apps.maps",
						"com.google.android.maps.MapsActivity");
				startActivity(intent);
			}
		});

		rellayout_back_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkIfTagNameChanged();
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				finish();
				startActivity(intent);
			}
		});
		btn_show_OK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkIfTagNameChanged();
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				finish();
				startActivity(intent);
			}
		});
	}

	private void checkIfTagNameChanged() {
		if (!(txt_show_TAG.getText().toString().equals(mBundle
				.getString("TagName")))) {
			try {
				ContentValues cv = new ContentValues();
				cv.put(DatabaseHelper.COL_TAGNAME, txt_show_TAG.getText()
						.toString());
				db.update(
						DatabaseHelper.TAGNFIND_LOCATION,
						cv,
						DatabaseHelper.COL_TAGNAME + "='"
								+ mBundle.getString("TagName") + "'", null);
				Toast.makeText(
						mContext,
						"Successfully updated tag name to "
								+ txt_show_TAG.getText().toString() + ".", 5000)
						.show();
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(mContext, "Unable to update Tag Name.", 5000)
						.show();
			}

		}
	}

	private Boolean displayGpsStatus() {
		ContentResolver contentResolver = getBaseContext().getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(
				contentResolver, LocationManager.GPS_PROVIDER);
		if (gpsStatus) {
			return true;

		} else {
			return false;
		}
	}

	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			System.out.println(loc.getLatitude() + "::" + loc.getLongitude()
					+ "::" + loc.getAccuracy());
			GpsFixWait.dismiss();
			longitude = String.valueOf(loc.getLongitude());
			latitude = String.valueOf(loc.getLatitude());
			System.out
					.println("MainActivity.MyLocationListener.onLocationChanged()");
			locationManager.removeUpdates(locationListener);
			/*
			 * Toast.makeText( getBaseContext(), "Location changed : Lat: " +
			 * loc.getLatitude() + " Lng: " + loc.getLongitude(),
			 * Toast.LENGTH_SHORT).show();
			 */

			/*----------to get City-Name from coordinates ------------- */

			/*
			 * Geocoder gcd = new Geocoder(getBaseContext(),
			 * Locale.getDefault()); List<Address> addresses; try { addresses =
			 * gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); if
			 * (addresses.size() > 0)
			 * System.out.println(addresses.get(0).getLocality()); // cityName
			 * // = addresses.get(0).getLocality(); String cityName =
			 * addresses.get(0).getAddressLine(1);
			 * txtDescription.setText(cityName); ContentValues cv = new
			 * ContentValues(); cv.put(DatabaseHelper.COL_ADDRESS, cityName);
			 * cv.put(DatabaseHelper.COL_LAT, loc.getLatitude());
			 * cv.put(DatabaseHelper.COL_LONG, loc.getLongitude());
			 * db.update(DatabaseHelper.TAGNFIND_LOCATION, cv,
			 * DatabaseHelper.COL_TAGNAME + "='" +
			 * txt_show_TAG.getText().toString() + "'", null);
			 * Toast.makeText(mContext, "Location updated to :" + cityName,
			 * 5000).show(); } catch (IOException e) { e.printStackTrace();
			 * Toast.makeText(mContext, "Unable to update Tag Location.", 5000)
			 * .show(); }
			 */

			gtlocation = new GetLocation(mContext, loc.getLatitude(),
					loc.getLongitude(), new LocationCallback() {

						@Override
						public void run(String result) {
							// TODO Auto-generated method stub
							if (result != null) {
								String cityName = result;
								String s = longitude + "\n" + latitude
										+ "\n\nMy Currrent City is: "
										+ cityName;
								txtDescription.setText(cityName);
								ContentValues cv = new ContentValues();
								try {

									cv.put(DatabaseHelper.COL_ADDRESS, cityName);
									cv.put(DatabaseHelper.COL_LAT, latitude);
									cv.put(DatabaseHelper.COL_LONG, longitude);
									db.update(DatabaseHelper.TAGNFIND_LOCATION,
											cv, DatabaseHelper.COL_TAGNAME
													+ "='"
													+ txt_show_TAG.getText()
															.toString() + "'",
											null);
									Toast.makeText(mContext,
											"Location updated to :" + cityName,
											5000).show();
								} catch (Exception e) { // TODO: handle
									// exception
									// System.out.println(e.toString());
									Toast.makeText(mContext,
											"Unable to Insert Data", 3000)
											.show();
								}
								Toast.makeText(mContext,
										"Location updated to :" + cityName,
										3000).show();
							} else {
								Toast.makeText(
										mContext,
										"Your location is currently unavailable. Please wait for GPS to connect and then try again.",
										3000).show();
							}

						}
					});

			gtlocation.execute("");

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		db.close();
	}
}
