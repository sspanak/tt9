package org.nyanya.android.traditionalt9;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class DBUpdateService extends IntentService {
	private static final int UPDATING_NOTIFICATION_ID = 9640142;

	private Handler mHandler;

	// http://stackoverflow.com/a/20690225
	public class DisplayToast implements Runnable {
		private final Context mContext;
		CharSequence mText;

		public DisplayToast(Context mContext, CharSequence text){
			this.mContext = mContext;
			mText = text;
		}

		public void run(){
			Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
		}
	}


	public DBUpdateService() {
		super("DBUpdateService");
		mHandler = new Handler();
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// do things
		T9DB dbhelper = T9DB.getInstance(this);
		if (dbhelper.getSQLDB(this) != null) {
			return;
		}
		Log.d("T9DBUpdate.onHandle", "Update pass check.");
		// do real things
		Notification notification = new Notification(R.drawable.ime_en_lang_lower, getText(R.string.updating_database_title),
				System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, DBUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, getText(R.string.updating_database_title),
				getText(R.string.updating_database), pendingIntent);
		startForeground(UPDATING_NOTIFICATION_ID, notification);



		//put this in a thread
		mHandler.post(new DisplayToast(this, getText(R.string.updating_database)));

		SQLiteDatabase tdb = dbhelper.getWritableDatabase();
		dbhelper.setSQLDB(tdb);
		mHandler.post(new DisplayToast(this, getText(R.string.updating_database_done)));
		Log.d("T9DBUpdate.onHandle", "done.");
	}
}
