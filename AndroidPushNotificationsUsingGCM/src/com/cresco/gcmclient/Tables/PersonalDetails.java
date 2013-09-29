package com.cresco.gcmclient.Tables;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.cresco.gcmclient.Services.DbHelper;

public class PersonalDetails extends Application
{
	private static String TAG = "PersonalDetails";

	Context context;

	public static String TABLE_NAME	= "personal_details";

	public static String _ID			= "_id";
	public static String REG_ID			= "reg_id";
	public static String IS_REGISTERED	= "is_registered";
	
	DbHelper dbHelper;
	SQLiteDatabase db;

	public PersonalDetails(Context context)
	{
		this.context = context;
		dbHelper = DbHelper.getInstance(context);
	}
	
	public void CRUD(ContentValues cv)
	{
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		
		db.insert(TABLE_NAME, null, cv);
	}
	
	public void setRegisteredFlag(String registered)
	{
		db = dbHelper.getWritableDatabase();
		String sql = "INSERT INTO "+PersonalDetails.TABLE_NAME + 
						"("+ IS_REGISTERED +") VALUES ('" + registered + "');";
		Log.v(TAG, "PD sql: "+sql);
		
		db.execSQL(sql);
	}
	
	public void setRegId (String regId)
	{
		db = dbHelper.getWritableDatabase();
		String sql = "UPDATE "+PersonalDetails.TABLE_NAME + 
						" SET "+ REG_ID +" = '" + regId + "';";
		
		Log.v(TAG, "PD sql: "+sql);
		db.execSQL(sql);
		
		Log.v(TAG, "Reg ID sql: "+getRegId());
	}
	
	public String getRegisteredFlag()
	{
		db = dbHelper.getWritableDatabase();
		String sql = "SELECT "+ IS_REGISTERED + " FROM " +PersonalDetails.TABLE_NAME;
		String flag = "";
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if(cursor.moveToFirst())
		{
			flag = cursor.getString(cursor.getColumnIndex(PersonalDetails.IS_REGISTERED));
			Log.v(TAG, "Flag is:" + flag);
		}
		if(flag == null)
		{
			flag = "N";
		}
		
		return flag;
	}
	
	public String getRegId()
	{
		db = dbHelper.getWritableDatabase();
		String sql = "SELECT "+ REG_ID + " FROM " +PersonalDetails.TABLE_NAME;
		String regId = "";
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if(cursor.moveToFirst())
		{
			regId = cursor.getString(cursor.getColumnIndex(PersonalDetails.REG_ID));
			Log.v(TAG, "Reg ID is:" + regId);
		}
		
		if(regId == null)
		{
			regId = "N";
		}
		
		return regId;
	}
}
