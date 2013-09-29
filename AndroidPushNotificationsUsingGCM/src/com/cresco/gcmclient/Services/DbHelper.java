package com.cresco.gcmclient.Services;

import java.io.IOException;
import java.util.ArrayList;

import com.cresco.gcmclient.Tables.ChatHistory;
import com.cresco.gcmclient.Tables.GCMUsers;
import com.cresco.gcmclient.Tables.PersonalDetails;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DbHelper extends SQLiteOpenHelper 
{
	protected static final String DATABASE_NAME = "gcm.db";
	protected static final int DATABASE_VERSION = 3;
	protected static final String TAG = "DbHelper";
	private static final String DB_FILEPATH = "/data/data/com.pushnotifications/databases/gcm.db";
	private static DbHelper mInstance = null;
	public Context context;

	static int dbVersion = 2 ;

	public DbHelper(Context context, String name, CursorFactory factory,int version) 
	{
		super(context, name, factory, version);
	}

	public DbHelper(Context c,int version)
	{
		super(c, DATABASE_NAME, null, version);
		context = c;
	}

	public static DbHelper getInstance(Context c)
	{
		if(mInstance == null)
		{
			mInstance =  new DbHelper(c, dbVersion);
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.v(TAG,"In Oncreate of DBHelper");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + GCMUsers.TABLE_NAME +
				"(" +
				GCMUsers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				GCMUsers.DISPLAY_NAME	+ " TEXT," +
				GCMUsers.MOBILE_NUM	+ " TEXT," +
				GCMUsers.REG_ID		+" TEXT)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + PersonalDetails.TABLE_NAME +
				"(" +
				PersonalDetails._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				PersonalDetails.REG_ID + " TEXT," +
				PersonalDetails.IS_REGISTERED +" TEXT)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ChatHistory.TABLE_NAME +
				"(" +
				ChatHistory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				ChatHistory.REG_ID  + " TEXT," +
				ChatHistory.MESSAGE	+ " TEXT," +
				ChatHistory.DATE	+ " TEXT," +
				ChatHistory.TIME	+ " TEXT," + 
				ChatHistory.MSG_TYPE	+ " CHAR," +
				ChatHistory.DELIVERY_STATUS +" TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.v(TAG,"upgrade database from {} to {}" + oldVersion +" to " + newVersion);
	}

	protected void execSqlFile(String sqlFile, SQLiteDatabase db ) throws SQLException, IOException
	{

	}

	public boolean importDatabase(String dbPath) throws IOException
	{
		close();
		return false;
	}

	public  void addRecords(SQLiteDatabase db)
	{

	}
}