package org.nyanya.android.traditionalt9;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
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
		Intent notificationIntent = new Intent(this, DBUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Notification.Builder nBuilder = new Notification.Builder(this);
		Notification notification = nBuilder
				.setContentIntent(pendingIntent)
				.setContentTitle(getText(R.string.updating_database_title))
				.setContentText(getText(R.string.updating_database))
				.setSmallIcon(R.drawable.ime_en_lang_lower)
				.getNotification();

		notificationManager.notify(UPDATING_NOTIFICATION_ID, notification);

		startForeground(UPDATING_NOTIFICATION_ID, notification);

		//put this in a thread
		mHandler.post(new DisplayToast(this, getText(R.string.updating_database)));

		SQLiteDatabase tdb = dbhelper.getWritableDatabase();
		dbhelper.setSQLDB(tdb);
		mHandler.post(new DisplayToast(this, getText(R.string.updating_database_done)));
		Log.d("T9DBUpdate.onHandle", "done.");
	}
}
