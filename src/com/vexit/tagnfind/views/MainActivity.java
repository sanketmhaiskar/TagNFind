package com.vexit.tagnfind.views;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vexit.tagnfind.R;
import com.vexit.tagnfind.adapters.TagListAdapter;
import com.vexit.tagnfind.asynctasks.GetLocation;
import com.vexit.tagnfind.databases.DatabaseHelper;
import com.vexit.tagnfind.objects.CheckNetwork;
import com.vexit.tagnfind.objects.LocationCallback;
import com.vexit.tagnfind.objects.TagResults;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

	EditText searchtext;
	static RelativeLayout searchbox;
	LocationManager locationManager = null;
	LocationListener locationListener = null;
	ListView list;
	static ArrayList<TagResults> arraylist_results;
	ArrayList<TagResults> partialNames = new ArrayList<TagResults>();
	TagResults er;
	static TagListAdapter adapter;
	ProgressDialog ShowProgress;
	static final String TAG = "Debug";
	Boolean flag = false;
	static Context mContext;
	static SQLiteDatabase db = null;
	RelativeLayout btnTAG, btnHELP;
	DatabaseHelper dbhelper;
	Listener gpsListener;
	static Cursor cursor = null;
	GetLocation gtlocation;
	ContentValues tag_values;

	private int MAX_TIMES_WAIT = 25;
	private static int MAX_ITEMS_SEARCH = 9;
	ProgressDialog GpsFixWait;
	static int checkThreshhold = 0;
	String cityName = null;
	String longitude = null, latitude = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this;
		locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		init();

		dbhelper = new DatabaseHelper(this);
		db = dbhelper.getWritableDatabase();
		db.setLockingEnabled(true);

		cursor = null;
		cursor = db.rawQuery("select * from "
				+ DatabaseHelper.TAGNFIND_LOCATION, null);
		if (cursor.getCount() <= 0) {
			cursor.close();

		} else {
			cursor.close();
			getTagsfromDB();
			adapter = new TagListAdapter(getApplicationContext(),
					arraylist_results);
			list.setAdapter(adapter);
		}

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Object obj_ret = list.getItemAtPosition(position);
				TagResults result_obj = (TagResults) obj_ret;
				String temp = result_obj.getTagName();
				Intent intent = new Intent(getApplicationContext(),
						ViewTagActivity.class);
				intent.putExtra("TagName", result_obj.getTagName());
				intent.putExtra("Address", result_obj.getAddress());
				System.out.println(result_obj.getAddress());
				intent.putExtra("Latitude", result_obj.getLatitude());
				intent.putExtra("Longitude", result_obj.getLongitude());
				finish();
				startActivity(intent);

			}
		});

	}

	public void dataSetChanged() {
		getTagsfromDB();
		adapter.notifyDataSetChanged();
	}

	public void getTagsfromDB() {
		// TODO Auto-generated method stub
		cursor = null;
		cursor = db.rawQuery("select * from "
				+ DatabaseHelper.TAGNFIND_LOCATION, null);
		System.err.println("COUNT " + cursor.getCount());
		arraylist_results = new ArrayList<TagResults>();
		er = new TagResults();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();

			er.setAddress((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COL_ADDRESS))));
			er.setLatitude((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COL_LAT))));
			er.setLongitude((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COL_LONG))));
			er.setTagName((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COL_TAGNAME))));
			er.setAddress((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COL_ADDRESS))));
			arraylist_results.add(er);
			er = new TagResults();
		}
		if (cursor.getCount() > MAX_ITEMS_SEARCH) {
			searchbox.setVisibility(View.VISIBLE);
		} else {
			searchbox.setVisibility(View.GONE);
		}
		cursor.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	@SuppressLint({ "NewApi", "DefaultLocale" })
	private void AlterAdapter() {

		if (searchtext.getText().toString().isEmpty()) {
			partialNames.clear();
			partialNames.addAll(arraylist_results);
			/* adapter.notifyDataSetChanged(); */
			adapter = new TagListAdapter(getApplicationContext(), partialNames);
			adapter.notifyDataSetChanged();
			list.setAdapter(adapter);

		} else {
			partialNames.clear();
			for (int i = 0; i < arraylist_results.size(); i++) {

				if (arraylist_results
						.get(i)
						.getTagName()
						.toUpperCase()
						.contains(searchtext.getText().toString().toUpperCase())
						|| arraylist_results
								.get(i)
								.getTagName()
								.toUpperCase()
								.startsWith(
										searchtext.getText().toString()
												.toUpperCase())) {
					partialNames.add(arraylist_results.get(i));
				}

			}
			System.out.println(partialNames.size());
			adapter = new TagListAdapter(getApplicationContext(), partialNames);
			adapter.notifyDataSetChanged();
			list.setAdapter(adapter);

		}
	}

	public void init() {
		list = (ListView) findViewById(R.id.taglist);
		searchbox = (RelativeLayout) findViewById(R.id.searchbox);
		searchtext = (EditText) findViewById(R.id.searchtext);
		btnTAG = (RelativeLayout) findViewById(R.id.rellayout_TAG_btn);
		btnHELP = (RelativeLayout) findViewById(R.id.rellayout_HELP_btn);
		searchtext.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				AlterAdapter();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				/*
				 * btnRefresh.setVisibility(View.INVISIBLE);
				 * btnDone.setVisibility(View.VISIBLE);
				 */
			}
		});
		btnTAG.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * ShowProgress = new ProgressDialog(mContext);
				 * ShowProgress.setMessage("Getting the Location..");
				 * ShowProgress.setCancelable(true); ShowProgress
				 * .setOnCancelListener(new DialogInterface.OnCancelListener() {
				 * 
				 * @Override public void onCancel(DialogInterface dialog) { try
				 * { System.out.println("Inside cancel");
				 * Toast.makeText(mContext, "Loading Cancelled.", 3000).show();
				 * Log.i("TASK", "Cancelled"); } catch (Exception e) { // TODO:
				 * handle exception }
				 * 
				 * } });
				 * 
				 * ShowProgress.show();
				 */
				if (CheckNetwork.isNetworkAvailable(mContext)) {
					flag = displayGpsStatus();
					if (flag) {

						final Dialog dialog = new Dialog(mContext);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setContentView(R.layout.tag_popup);

						final EditText edtxt_target_name = (EditText) dialog
								.findViewById(R.id.edtxt_target_name);

						final Button btnCancel = (Button) dialog
								.findViewById(R.id.btn_popup_cancel);
						final Button btnOK = (Button) dialog
								.findViewById(R.id.btn_popup_OK);
						btnCancel
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method
										// stub
										dialog.dismiss();
									}
								});

						btnOK.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								// Insert into Database

								tag_values = new ContentValues();
								tag_values.put(DatabaseHelper.COL_TAGNAME,
										edtxt_target_name.getText().toString());

								dialog.dismiss();
								GpsFixWait = new ProgressDialog(mContext);
								GpsFixWait
										.setMessage("Waiting for location...");
								GpsFixWait.setCancelable(true);
								GpsFixWait
										.setOnCancelListener(new DialogInterface.OnCancelListener() {

											@Override
											public void onCancel(
													DialogInterface dialog) {
												// TODO Auto-generated method
												// stub
												System.out.println("CANCELLED");
												locationManager
														.removeGpsStatusListener(gpsListener);
												locationManager
														.removeUpdates(locationListener);

											}
										});
								GpsFixWait.show();

								locationListener = new MyLocationListener();

								checkThreshhold = 0;
								locationManager.requestLocationUpdates(
										LocationManager.GPS_PROVIDER, 500, 1,
										locationListener);

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
											// Fix received ,remove gpslistener
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
													+ checkThreshhold + "::"
													+ "4");
											if (checkThreshhold > MAX_TIMES_WAIT) {
												// Unable to get a fix so use
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
							}
						});
						dialog.show();

					} else {
						Toast.makeText(getApplicationContext(),
								"Please turn the GPS ON", 5000).show();
					}
				} else {
					Toast.makeText(
							getApplicationContext(),
							"No internet connection. Please connect to internet to use GPS.",
							5000).show();
				}

			}
		});
		btnHELP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						HelpActivity.class);
				finish();
				startActivity(intent);
			}
		});
	}

	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			System.out.println(loc.getLatitude() + "::" + loc.getLongitude()
					+ "::" + loc.getAccuracy());
			GpsFixWait.dismiss();
			System.out
					.println("MainActivity.MyLocationListener.onLocationChanged()");
			locationManager.removeGpsStatusListener(gpsListener);
			locationManager.removeUpdates(locationListener);
			/*
			 * Toast.makeText( getBaseContext(), "Location changed : Lat: " +
			 * loc.getLatitude() + " Lng: " + loc.getLongitude(),
			 * Toast.LENGTH_SHORT).show();
			 */
			longitude = String.valueOf(loc.getLongitude());
			latitude = String.valueOf(loc.getLatitude());
			gtlocation = new GetLocation(mContext, loc.getLatitude(),
					loc.getLongitude(), new LocationCallback() {

						@Override
						public void run(String result) {
							// TODO Auto-generated method stub
							if (result != null) {
								cityName = result;
								String s = longitude + "\n" + latitude
										+ "\n\nMy Currrent City is: "
										+ cityName;

								try {

									tag_values.put(DatabaseHelper.COL_LAT,
											latitude);
									tag_values.put(DatabaseHelper.COL_LONG,
											longitude);
									tag_values.put(DatabaseHelper.COL_ADDRESS,
											cityName);
									db.insertOrThrow(
											DatabaseHelper.TAGNFIND_LOCATION,
											null, tag_values);
									getTagsfromDB();
									adapter = new TagListAdapter(
											getApplicationContext(),
											arraylist_results);
									list.setAdapter(adapter);

								} catch (Exception e) { // TODO: handle
									// exception
									// System.out.println(e.toString());
									Toast.makeText(mContext,
											"Unable to Insert Data", 3000)
											.show();
								}
								Toast.makeText(
										mContext,
										tag_values.getAsString(DatabaseHelper.COL_TAGNAME) + " @ "
												+ cityName, 3000).show();
							} else {
								Toast.makeText(
										mContext,
										"Your location is currently unavailable. Please wait for GPS to connect and then try again.",
										3000).show();
							}

						}
					});

			gtlocation.execute("");
			/*----------to get City-Name from coordinates ------------- */

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

	public static void deleteEvent(final String tagName, final String address,
			final int position) {
		// TODO Auto-generated method stub
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					arraylist_results.remove(position);
					adapter.notifyDataSetChanged();
					int row = db.delete(DatabaseHelper.TAGNFIND_LOCATION,
							DatabaseHelper.COL_TAGNAME + "='" + tagName
									+ "' AND " + DatabaseHelper.COL_ADDRESS
									+ "='" + address + "'", null);
					if (arraylist_results.size() > MAX_ITEMS_SEARCH) {
						searchbox.setVisibility(View.VISIBLE);
					} else {
						searchbox.setVisibility(View.GONE);
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
		builder.setMessage("Delete tag for : " + tagName + " ?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public static void navigateLocation(String uri) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse(uri));
		intent.setClassName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity");
		mContext.startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		db.close();
		super.onBackPressed();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		db.close();
		super.onDestroy();
	}
}
