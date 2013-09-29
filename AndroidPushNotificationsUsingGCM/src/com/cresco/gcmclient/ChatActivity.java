package com.cresco.gcmclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cresco.gcmclient.Services.JSONParser;
import com.cresco.gcmclient.Tables.ChatHistory;
import com.cresco.gcmclient.Tables.GCMUsers;

public class ChatActivity extends Activity implements OnClickListener, OnItemLongClickListener
{	
	public static String TAG = ChatActivity.class.getSimpleName();

	String ls_mobile, ls_name, ls_regIdTo, ls_regIdFrom;

	public static boolean activityRunning = false;

	public static String TAG_MESSAGE 		= "message";
	public static String TAG_SUCCESS 		= "success";
	public static String TAG_REG_ID_FROM 	= "reg_id_from";
	public static String TAG_REG_ID_TO 		= "reg_id_to";
	public static String TAG_MSG_ID 		= "msg_id";
	public static String TAG_DELIVERY_ID 	= "delivered";

	public static String URL_SEND_MESSAGE 		= "http://www.blazoorder.com/blazoorder/send_message.php";
	public static String URL_MESSAGE_HISTORY 	= "http://www.blazoorder.com/blazoorder/message_history.php";

	Context context;

	Button btn_send;
	EditText et_msg;
	TextView tv_displayName;

	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();

	// Connection detector
	ConnectionDetector cd;

	MessageReceiver mReceiver = new MessageReceiver();

	String[] ls_from = {"incoming", "incomingTime", "outgoing", "outgoingTime"};
	int[] li_mapping = {R.id.tv_incomingMsg, R.id.tv_incTime, R.id.tv_outgoingMsg, R.id.tv_outTime};

	ChatHistory history;

	SimpleCursorAdapter adapter;
	ListView lv_chats;

	long ll_msgId;
	String delivered;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chat_activity);
		context = this;

		ls_mobile 		= getIntent().getStringExtra(GCMUsers.MOBILE_NUM);
		ls_name 		= getIntent().getStringExtra(GCMUsers.DISPLAY_NAME);
		ls_regIdTo 		= getIntent().getStringExtra(ChatActivity.TAG_REG_ID_TO);
		ls_regIdFrom 	= getIntent().getStringExtra(ChatActivity.TAG_REG_ID_FROM);

		btn_send = (Button)findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);

		et_msg = (EditText) findViewById(R.id.et_message);
		tv_displayName = (TextView) findViewById(R.id.tv_displayName);
		tv_displayName.setText(ls_name);

		btn_send.setEnabled(false);
		et_msg.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				if(et_msg.getText().toString().length() > 0)
				{
					btn_send.setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
		});

		et_msg.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) 
			{
				if(actionId == EditorInfo.IME_ACTION_SEND)
				{
					cd = new ConnectionDetector(getApplicationContext());
					if (!cd.isConnectingToInternet()) 
					{
						Log.v(TAG, "Entered here. ");
						// Internet Connection is not present
						alert.showAlertDialog(ChatActivity.this,
								"Internet Connection Error",
								"Please connect to working Internet connection", false);
					}

					else
					{
						sendMsg();
						refreshList();
					}

				}
				return false;
			}

		});

		history = new ChatHistory(context);

		lv_chats = (ListView) findViewById(R.id.lv_displayChats);
		lv_chats.setOnItemLongClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		case R.id.btn_send:

			cd = new ConnectionDetector(getApplicationContext());
			if (!cd.isConnectingToInternet()) 
			{
				// Internet Connection is not present
				alert.showAlertDialog(ChatActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
			}
			else
			{
				sendMsg();
				refreshList();
			}
			break;
		}
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		activityRunning = true;

		// registering the receiver
		IntentFilter filter = new IntentFilter("com.cresco.refresh_list");

		registerReceiver(mReceiver, filter);
		refreshList();
	}

	public void sendMsg()
	{
		String msgToSend = et_msg.getText().toString();
		String date, time;

		SimpleDateFormat sdf = new SimpleDateFormat();
		date = sdf.format(new Date());

		Calendar calendar = Calendar.getInstance();
		int hour 	= calendar.get(Calendar.HOUR_OF_DAY);
		int minute 	= calendar.get(Calendar.MINUTE);

		// inserting row into chat_history
		ContentValues cv = new ContentValues();

		cv.put(ChatHistory.MSG_TYPE, "O");
		cv.put(ChatHistory.REG_ID, ls_regIdTo);
		cv.put(ChatHistory.MESSAGE, msgToSend);
		cv.put(ChatHistory.DATE, date);
		cv.put(ChatHistory.TIME, hour+ ":"+minute);

		ChatHistory chatHistory = new ChatHistory(context);
		ll_msgId = chatHistory.CRUD(cv);

		new SendMsgTask().execute();

		et_msg.setText("");
		btn_send.setEnabled(false);

		((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE))
		.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
	}
	class SendMsgTask extends AsyncTask<String, String, String>
	{
		String regId, msgToSend, msgFromServer, name;
		JSONParser jsonParser;

		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();

			msgToSend 	= et_msg.getText().toString();
		}

		@Override
		protected String doInBackground(String... result)
		{
			JSONObject jsonObject;
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair(TAG_MESSAGE		, msgToSend + "reg_id_from" + ls_regIdFrom ));
			params.add(new BasicNameValuePair(TAG_REG_ID_TO 	, ls_regIdTo));
			params.add(new BasicNameValuePair(TAG_REG_ID_FROM 	, ls_regIdFrom));
			params.add(new BasicNameValuePair(TAG_MSG_ID 		, "" + ll_msgId));

			jsonParser = new JSONParser();

			jsonObject = jsonParser.makeHttpRequest(URL_SEND_MESSAGE, "GET", params);

			try
			{
				if(jsonObject != null)
				{

					if(jsonObject.getInt(TAG_SUCCESS) == 1)
					{
						Log.v(TAG, "Success 1, msg: "+msgFromServer);

						ChatHistory chatHistory = new ChatHistory(context);
						chatHistory.setDeliveredField("Y", ls_regIdTo, ll_msgId);
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

			ChatHistory chatHistory = new ChatHistory(context);
			delivered = chatHistory.getDeliveredField(ls_regIdTo, ""+ll_msgId);

			params.add(new BasicNameValuePair(TAG_DELIVERY_ID, delivered));

			jsonObject = jsonParser.makeHttpRequest(URL_MESSAGE_HISTORY, "POST", params);

			try
			{
				if(jsonObject != null)
				{

					if(jsonObject.getInt(TAG_SUCCESS) == 1)
					{
						Log.v(TAG, "Oh YES! Success 1, msg: "+msgFromServer);
					}
					else
					{
						Log.v(TAG, "Oh NO! Success 0, msg: "+msgFromServer);
					}
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
			GetGCMContacts.copyDB();
		}
	}

	public void refreshList()
	{
		Cursor cursor = history.getMessagesById(ls_regIdTo);
		adapter = new SimpleCursorAdapter(context, R.layout.chat_row, cursor, ls_from, li_mapping);
		lv_chats.setAdapter(adapter);
		lv_chats.invalidateViews();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id)
	{
		if (view.getId() == R.id.tv_incomingMsg || view.getId() == R.id.tv_outgoingMsg)
		{
			TextView tv = (TextView) view;

			if (tv != null)
			{
				registerForContextMenu(tv);
			}
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		// user has long pressed your TextView
		menu.add(0, v.getId(), 0,"Copy");

		TextView textView = (TextView) v;

		if (textView != null)
		{
			@SuppressWarnings("deprecation")
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(textView.getText());
		}
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
		activityRunning = false;
		unregisterReceiver(mReceiver);
	}

	class MessageReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			refreshList();
		}		
	}
}
