package com.cresco.gcmclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import com.cresco.gcmclient.Services.FileUtils;
import com.cresco.gcmclient.Services.JSONParser;
import com.cresco.gcmclient.Tables.GCMUsers;

public class GetGCMContacts extends Application
{
	String TAG = getClass().getSimpleName();

	Context context;

	static String TAG_SUCCESS	 = "success";
	static String TAG_MESSAGE 	 = "message";
	static String TAG_CONTACTS	 = "contacts";
	static String TAG_NAME 	 	 = "name";
	static String TAG_MOB_NUM	 = "mobile";
	static String TAG_REGID  	 = "reg_id";

	static String URL_GET_ALL_USERS = "http://www.blazoorder.com/blazoorder/get_all_users.php";

	List<String[]> localContacts, serverContacts;

	public GetGCMContacts(Context context)
	{
		this.context = context;
	}

	public void getContactsFromServer()
	{
		localContacts = getLocalContacts();
		new SendMsgTask().execute();
	}

	class SendMsgTask extends AsyncTask<String, String, String>
	{
		String regId, msgToSend, msgFromServer;
		JSONParser jsonParser;

		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			Toast.makeText(context, "Sending Message...", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected String doInBackground(String... result) 
		{
			JSONObject jsonObject;
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("", ""));

			jsonParser = new JSONParser();

			jsonObject = jsonParser.makeHttpRequest(URL_GET_ALL_USERS, "GET", params);

			try
			{
				if(jsonObject != null)
				{

					if(jsonObject.getInt(TAG_SUCCESS) == 1)
					{
						Log.v(TAG, "Success 1, msg: "+msgFromServer);
						JSONArray jsonArray = jsonObject.getJSONArray(TAG_CONTACTS);
						JSONObject jObject;
						String[] contact;

						serverContacts = new ArrayList<String[]>();

						for(int i=0 ; i<jsonArray.length() ; i++)
						{
							jObject = (JSONObject) jsonArray.get(i);

							contact = new String[3];
							contact[0] = jObject.getString(TAG_NAME);
							contact[1] = jObject.getString(TAG_MOB_NUM);
							contact[2] = jObject.getString(TAG_REGID);

							serverContacts.add(contact);
						}

						for (int i=0 ; i < serverContacts.size() ; i++)
						{
							Log.v(TAG, "Name: "+serverContacts.get(i)[0] + " and no.: " + serverContacts.get(i)[1]
									+ "Reg_id: " + serverContacts.get(i)[2]);
						}
					}
					else
					{
						Log.v(TAG, "Success 0, msg: "+msgFromServer);
					}

					msgFromServer = jsonObject.getString(TAG_MESSAGE);
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}

			return null;
		}


		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);			
			Toast.makeText(context, msgFromServer, Toast.LENGTH_SHORT).show();

			List<String[]> matchingContacts = getMatchingContacts(localContacts, serverContacts);
			storeMatchingContacts(matchingContacts);
		}
	}

	public List<String[]> getLocalContacts()
	{
		Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		String[] contact;
		List<String[]> contacts = new ArrayList<String[]>();
		int i=0;

		while (phones.moveToNext())
		{
			contact = new String[2];

			String name 	= phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String number	= phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			contact[0] = name;
			contact[1] = number;
			Log.v(TAG, "name: "+name + " and number: "+number);

			contacts.add(i, contact);

			i++;
		}

		return contacts;
	}

	public List<String[]> getMatchingContacts(List<String[]> localContacts, List<String[]> serverContacts)
	{
		List<String[]> matchingContacts = new ArrayList<String[]>();

		for(int i=0 ; i<localContacts.size() ; i++)
		{
			for(int j=0 ; j<serverContacts.size() ; j++)
			{
				if(PhoneNumberUtils.compare(localContacts.get(i)[1], serverContacts.get(j)[1]))
				{
					String[] contact = new String[3];
					contact[0] = localContacts.get(i)[0];
					contact[1] = localContacts.get(i)[1];
					contact[2] = serverContacts.get(j)[2];

					matchingContacts.add(contact);

					Log.v(TAG, " Hurrah! name: "+localContacts.get(i)[0] + " and number: "+localContacts.get(i)[1]);
					break;
				}
			}
		}

		return matchingContacts;
	}

	public void storeMatchingContacts(List<String[]> matchingContacts)
	{
		ContentValues[] cv = new ContentValues[matchingContacts.size()];

		for(int i=0 ; i<matchingContacts.size() ; i++)
		{
			cv[i] = new ContentValues();

			cv[i].put(GCMUsers.DISPLAY_NAME, matchingContacts.get(i)[0]);
			cv[i].put(GCMUsers.MOBILE_NUM  , matchingContacts.get(i)[1]);
			cv[i].put(GCMUsers.REG_ID  	   , matchingContacts.get(i)[2]);

			Log.v(TAG, "RegId: " +matchingContacts.get(i)[2]);
		}

		GCMUsers gcmUsers = new GCMUsers(context);
		gcmUsers.CRUD(cv);

		copyDB();
	}

	public static void copyDB()
	{
		File srcFile  = new File(Environment.getDataDirectory() + "/data/com.cresco.gcmclient/databases/gcm.db");
		File destFile = new File(Environment.getExternalStorageDirectory().toString() + "/gcm.db");

		try 
		{
			FileUtils.copyFile(new FileInputStream(srcFile), new FileOutputStream(destFile));
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
