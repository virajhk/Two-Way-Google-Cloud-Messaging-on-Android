package com.cresco.gcmclient.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cresco.gcmclient.GetGCMContacts;
import com.cresco.gcmclient.R;
import com.cresco.gcmclient.Tables.GCMUsers;
import com.cresco.gcmclient.Tables.PersonalDetails;
import com.cresco.gcmclient.RegisterActivity;
import com.cresco.gcmclient.GetGCMContacts;
import com.cresco.gcmclient.ChatActivity;

public class DisplayUsers extends Activity implements OnClickListener, OnItemClickListener
{
	public static String TAG = DisplayUsers.class.getSimpleName();
	
	String[] ls_from = {GCMUsers.DISPLAY_NAME};
	int[] li_mapping = {R.id.tv_rowname};
	
	Context context;
	
	Button lb_refresh;
	
	GCMUsers users ;
	
	SimpleCursorAdapter adapter;
	ListView lv_users;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		
		PersonalDetails pd = new PersonalDetails(this);
		String registered = pd.getRegisteredFlag();
		Log.v(TAG ,"registered: "+registered);
		
		if(registered == null || registered == "") registered = "N";
		
		setContentView(R.layout.display_users);
		lv_users = (ListView)findViewById(R.id.lv_displayNames);
		lb_refresh = (Button) findViewById(R.id.lb_refresh);

		if(registered.equals("N"))
		{
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivityForResult(intent, 1);
		}
		
		users = new GCMUsers(context);
		
		refreshList();
		
		lv_users.setOnItemClickListener(this);
		lb_refresh.setOnClickListener(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 1)
		{
			GetGCMContacts gcmContacts = new GetGCMContacts(context);
			gcmContacts.getContactsFromServer();
			refreshList();
		}
	}
	
	public void refreshList()
	{
		Cursor cursor = users.getAllUsers();
		adapter = new SimpleCursorAdapter(context, R.layout.display_users_row, cursor, ls_from, li_mapping);
		lv_users.setAdapter(adapter);
		lv_users.invalidateViews();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		GCMUsers gcmUsers	= new GCMUsers(context);
		PersonalDetails	pd	= new PersonalDetails(context);
		
		Cursor cursor = gcmUsers.getUserById(id);
		
		cursor.moveToFirst();

		String regIdTo 		= cursor.getString(cursor.getColumnIndex(GCMUsers.REG_ID));
		String mobile 		= cursor.getString(cursor.getColumnIndex(GCMUsers.MOBILE_NUM));
		String ls_name 		= cursor.getString(cursor.getColumnIndex(GCMUsers.DISPLAY_NAME));
		String regIdFrom 	= pd.getRegId();
		
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(GCMUsers.MOBILE_NUM, mobile);
		intent.putExtra(GCMUsers.DISPLAY_NAME,ls_name);
		intent.putExtra(ChatActivity.TAG_REG_ID_TO, regIdTo);
		intent.putExtra(ChatActivity.TAG_REG_ID_FROM, regIdFrom);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) 
	{
		if (v.getId() == R.id.lb_refresh)
		{
			Log.v(TAG, "Clicked.");
			refreshList();
		}
		
	}
}
