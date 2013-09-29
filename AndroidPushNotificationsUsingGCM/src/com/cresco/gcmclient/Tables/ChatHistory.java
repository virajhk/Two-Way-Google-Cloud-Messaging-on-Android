package com.cresco.gcmclient.Tables;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cresco.gcmclient.Services.DbHelper;

public class ChatHistory extends Application
{
	private static String TAG = "ChatHistory";

	Context context;

	public static String TABLE_NAME	= "chat_history";

	public static String _ID					= "_id";
	public static String REG_ID					= "reg_id";
	public static String MSG_TYPE				= "msg_type";
	public static String MESSAGE				= "message";
	public static String DATE					= "date";
	public static String TIME					= "time";
	public static String DELIVERY_STATUS		= "delivered";


	DbHelper dbHelper;
	SQLiteDatabase db;

	public ChatHistory(Context context)
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

	public long CRUD(ContentValues cv)
	{
		long ll_id = 0;
		
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();

		ll_id = db.insert(TABLE_NAME, null, cv);
		
		return ll_id;
	}

	public Cursor getMessagesById(String id)
	{
		Cursor cursor;
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();

		String ls_sql = "SELECT _id,reg_id,message,date,time, " +
						" case when msg_type = 'I' THEN  message ELSE '' END incoming ,"+
						" case when msg_type = 'I' THEN  time ELSE '' END incomingTime ,"+
						" case when msg_type = 'O' THEN  message ELSE '' END outgoing ,"+
						" case when msg_type = 'O' THEN  time ELSE '' END outgoingTime "+
						" FROM chat_history " +	
						" WHERE " + ChatHistory.REG_ID + " = '" + id + "';";
		
		Log.v(TAG, "sql: "+ls_sql);
		
		cursor = db.rawQuery(ls_sql, null);

		return cursor;
	}
	
	public void setDeliveredField(String delivered, String regId, long msgId)
	{
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		
		String sql = 	"UPDATE " + ChatHistory.TABLE_NAME + 
						" SET " + ChatHistory.DELIVERY_STATUS + " = '" + delivered + "' " +
						" WHERE " + ChatHistory.REG_ID + " = '" + regId+"'" +
						" AND " + ChatHistory._ID + " = " + msgId ;
		
		Log.v(TAG, "sql delivered: "+sql);
		Log.v(TAG, "delivered: "+ delivered + " for regid: "+regId);
		
		db.execSQL(sql);
	}
	
	public String getDeliveredField(String regId, String msgId)
	{
		dbHelper = DbHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		String delivered = "N";
		
		String sql = 	"SELECT * FROM " + ChatHistory.TABLE_NAME + 
						" WHERE " + ChatHistory.REG_ID + " = '" + regId+"'" +
						" AND " + ChatHistory._ID + " = " + msgId ;
		
		Cursor cursor = db.rawQuery(sql, null);
		
		if(cursor.moveToFirst())
		{
			delivered = cursor.getString(cursor.getColumnIndex(ChatHistory.DELIVERY_STATUS));
			
			if(delivered == null)
			{
				delivered = "N";
			}
		}
		
		return delivered;
	}
}
