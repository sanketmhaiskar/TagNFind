package com.vexit.tagnfind.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	Context context;
	public static final String DB_NAME = "com_vexit_tagnfind";
	static final int DB_VERSION = 1;

	public static final String TAGNFIND_LOCATION = "TAGNFIND_LOCATION";

	public static final String COL_TAGNAME = "Tagname";
	public static final String COL_LAT = "Latitude";
	public static final String COL_LONG = "Longitude";
	public static final String COL_ADDRESS = "Address";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + TAGNFIND_LOCATION + " (" + COL_TAGNAME
				+ " TEXT NOT NULL," + COL_LAT + " TEXT NOT NULL," + COL_LONG
				+ " TEXT NOT NULL," + COL_ADDRESS + " TEXT)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP " + TAGNFIND_LOCATION + " IF EXISTS");
	}
}
