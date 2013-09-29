package com.cresco.gcmclient;

import static com.cresco.gcmclient.CommonUtilities.SENDER_ID;
import static com.cresco.gcmclient.CommonUtilities.displayMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.ListView;

import com.cresco.gcmclient.Tables.ChatHistory;
import com.cresco.gcmclient.Tables.GCMUsers;
import com.cresco.gcmclient.Tables.PersonalDetails;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	
	static String regIdFrom;
	long ll_msgId;
	
	Context context = this;
	PersonalDetails pd = new PersonalDetails(context);
	String myRegId = pd.getRegId();
	

    public GCMIntentService() 
    {
        super(SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, "Your device registred with GCM");
        Log.d("NAME", "" + MainActivity.name);
        ServerUtilities.register(context, MainActivity.name, MainActivity.email, registrationId);
    }

    /**
     * Method called on device un registered
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("price");

        regIdFrom  = message.substring(message.indexOf("reg_id_from") + 11, message.length());
		message = message.substring(0, message.indexOf("reg_id_from"));
		
        receiveMsg(message, regIdFrom, context);
        
        displayMessage(context, message);
    }
    
    public void receiveMsg(String message, String regIdFrom, Context context)
	{
    	String date;
		SimpleDateFormat sdf = new SimpleDateFormat();
		date = sdf.format(new Date());
		
		Calendar calendar = Calendar.getInstance();
		int hour 	= calendar.get(Calendar.HOUR_OF_DAY);
		int minute 	= calendar.get(Calendar.MINUTE);
		
		// inserting row into chat_history
		ContentValues cv = new ContentValues();
		
		cv.put(ChatHistory.MSG_TYPE, "I");
		cv.put(ChatHistory.REG_ID, regIdFrom);
		cv.put(ChatHistory.MESSAGE, message);
		cv.put(ChatHistory.DATE, date);
		cv.put(ChatHistory.TIME, hour+ ":"+minute);
		
		ChatHistory chatHistory = new ChatHistory(context);
		ll_msgId = chatHistory.CRUD(cv);
		
		generateNotification(context, message);
		
		ChatActivity chatActivity = new ChatActivity();
		
		if(ChatActivity.activityRunning)
		{
			Intent intent = new Intent("com.cresco.refresh_list");
			context.sendBroadcast(intent);
		}
			
	}

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private void generateNotification(Context context, String message) 
    {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
		
        Notification notification = new Notification(icon, message, when);
        
        String title = context.getString(R.string.app_name);
        
        GCMUsers gcmUsers	= new GCMUsers(context);
		PersonalDetails	pd	= new PersonalDetails(context);
		
        Cursor cursor = gcmUsers.getUserByRegId(regIdFrom);
		
		cursor.moveToFirst();

		String regIdTo 		= pd.getRegId();
		String mobile 		= cursor.getString(cursor.getColumnIndex(GCMUsers.MOBILE_NUM));
		String ls_name 		= cursor.getString(cursor.getColumnIndex(GCMUsers.DISPLAY_NAME));
		
		Intent notificationIntent = new Intent(this, ChatActivity.class);
		notificationIntent.putExtra(GCMUsers.MOBILE_NUM, mobile);
		notificationIntent.putExtra(GCMUsers.DISPLAY_NAME,ls_name);
		notificationIntent.putExtra(ChatActivity.TAG_REG_ID_FROM, regIdTo);
		notificationIntent.putExtra(ChatActivity.TAG_REG_ID_TO, regIdFrom);
		
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
        
        //notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "your_sound_file_name.mp3");
        
        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);
    }

}
