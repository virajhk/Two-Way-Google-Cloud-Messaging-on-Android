package com.cresco.gcmclient.Tables;

import com.cresco.gcmclient.Services.DbHelper;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class GCMUsers extends Application
{
	private static String TAG = "GCMUsers";

	Context context;

	public static String TABLE_NAME	= "gcm_users";

	public static String _ID			= "_id";
	public static String DISPLAY_NAME	= "display_name";
	public static String MOBILE_NUM		= "mobile";
	public static String REG_ID			= "reg_id";
	
	DbHelper dbHelper;
	SQLiteDatabase db;

	public GCMUsers(Context context)
	{
		this.context = context;
	}
	
	public void CRUD(ContentValues[] cv)
	{
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		
		for(int i=0 ; i<cv.length ; i++)
		{
			db.insert(TABLE_NAME, null, cv[i]);
		}
	}
	
	public Cursor getAllUsers()
	{
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		String ls_sql = " SELECT * FROM "+GCMUsers.TABLE_NAME;
		Cursor cursor = db.rawQuery(ls_sql,null);
		return cursor;
	}
	
	public Cursor getUserById(long id)
	{
		Cursor cursor;
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		
		String sql = "SELECT * FROM "+ GCMUsers.TABLE_NAME + 
					 " WHERE " + GCMUsers._ID + " = "+ id;
		cursor = db.rawQuery(sql, null);
		
		return cursor;
	}
	
	public Cursor getUserByRegId(String regId)
	{
		Cursor cursor;
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		
		String sql = "SELECT * FROM "+ GCMUsers.TABLE_NAME + 
					 " WHERE " + GCMUsers.REG_ID+ " = '"+ regId + "'";
		cursor = db.rawQuery(sql, null);
		
		return cursor;
	}
}
