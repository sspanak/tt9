package org.nyanya.android.traditionalt9;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class TraditionalT9Settings extends PreferenceActivity implements
	DialogInterface.OnCancelListener {

	ProgressDialog pd = null;
	AsyncTask<String, Integer, Reply> task = null;
	final String dictname = "dict50-utf8.jet";
	final String backupname = "t9backup.txt";
	final String backupdir = "traditionalt9";

	final int BACKUP_Q_LIMIT = 1000;

	Context caller = null;

	private class Reply {
		public boolean status;
		public String message;

		protected Reply() {
			this.status = true;
			this.message = "None";
		}

	}

	private void closeStream(Closeable is, Reply reply) {
		if (is == null) {
			return;
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			reply.message = reply.message + "\n & Couldn't close stream: " + e.getMessage();
		}
	}

	private class LoadDictTask extends AsyncTask<String, Integer, Reply> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		@Override
		protected Reply doInBackground(String... mode) {
			Reply reply = new Reply();
			SQLiteDatabase db;
			db = T9DB.getSQLDB(caller);
			long fsize;
			long pos = 0;
			long last = 0;
			File backupfile = new File(Environment.getExternalStorageDirectory(), backupdir);

			if (mode[0].equals("backup")) {
				// using external backup
				backupfile = new File(backupfile, backupname);
				fsize = backupfile.length();
			} else {
				// using asset:
				AssetFileDescriptor descriptor;
				try {
					descriptor = getAssets().openFd(dictname);
					fsize = descriptor.getLength();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					reply.status = false;
					reply.message = e.getLocalizedMessage();
					db.close();
					return reply;
				}
			}

			db.setLockingEnabled(false);

			InsertHelper wordhelp = new InsertHelper(db, T9DB.WORD_TABLE_NAME);

			final int wordColumn = wordhelp.getColumnIndex(T9DB.COLUMN_WORD);
			final int freqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_FREQUENCY);
			final int seqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_SEQ);
			// add characters first, then dictionary:
			Log.d("doInBakground", "Adding characters...");
			for (Map.Entry<Character, Integer> entry : CharMap.CHARTABLE.entrySet()) {
				wordhelp.prepareForReplace();
				wordhelp.bind(seqColumn, Integer.toString(entry.getValue()));

				wordhelp.bind(wordColumn, Character.toString(entry.getKey()));
				wordhelp.bind(freqColumn, 0);
				wordhelp.execute();
			}
			Log.d("doInBakground", "done.");
			Log.d("doInBakground", "Adding dict...");
			BufferedReader br;
			InputStream dictstream = null;

			if (mode[0].equals("backup")) {
				try {
					dictstream = new FileInputStream(backupfile);
				} catch (FileNotFoundException e) {
					reply.status = false;
					reply.message = "Backup file not found: " + e.getMessage();
					db.close();
					closeStream(dictstream, reply); // this is silly but it
													// stops IDE nagging at me.
					return reply;
				}
			} else {
				try {
					dictstream = getAssets().open(dictname);
				} catch (IOException e) {
					e.printStackTrace();
					reply.status = false;
					reply.message = "IO Error: " + e.getMessage();
					db.close();
					return reply;
				}
			}

			try {
				br = new BufferedReader(new InputStreamReader(dictstream, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				reply.status = false;
				reply.message = "UnsupportedEncodingException Error: " + e.getMessage();
				db.close();
				closeStream(dictstream, reply);
				return reply;
			}

			String word;
			String[] ws;
			int freq;
			try {
				word = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				reply.status = false;
				reply.message = "IO Error: " + e.getMessage();
				db.close();
				closeStream(dictstream, reply);
				return reply;
			}
			String seq;
			int linecount = 1;
			int wordlen;

			long startnow, endnow;
			startnow = SystemClock.uptimeMillis();
			db.beginTransaction();
			try {
				while (word != null) {
					if (isCancelled()) {
						// this is useless because of dumb android bug that
						// doesn't call onCancelled after this method finishes.
						reply.status = false;
						reply.message = "User cancelled.";
						break;
					}
					if (word.contains(" ")) {
						ws = word.split(" ");
						word = ws[0];
						try {
							freq = Integer.parseInt(ws[1]);
						} catch (NumberFormatException e) {
							reply.status = false;
							reply.message = "Number error.";
							return reply;
						}
					} else {
						freq = 0;
					}
					linecount++;
					try {
						wordlen = word.getBytes("UTF-8").length;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						reply.status = false;
						reply.message = "UnsupportedEncodingException Error: " + e.getMessage();
						db.close();
						closeStream(dictstream, reply);
						return reply;
					}
					pos += wordlen;
					try {
						seq = CharMap.getStringSequence(word);
					} catch (NullPointerException e) {
						reply.status = false;
						reply.message = "Error on word: " + word;
						db.close();
						closeStream(dictstream, reply);
						return reply;
					}

					wordhelp.prepareForReplace();
					wordhelp.bind(seqColumn, seq);

					wordhelp.bind(wordColumn, word);
					wordhelp.bind(freqColumn, freq);
					wordhelp.execute();

					// System.out.println("Progress: " + pos + " Last: " + last
					// + " fsize: " + fsize);
					if ((pos - last) > 4096) {
						// Log.d("doInBackground", "line: " + linecount);
						// Log.d("doInBackground", "word: " + word);
						publishProgress((int) ((float) pos / fsize * 10000));
						last = pos;
					}
					try {
						word = br.readLine();
					} catch (IOException e) {
						e.printStackTrace();
						reply.status = false;
						reply.message = "IO Error: " + e.getMessage();
						db.close();
						closeStream(dictstream, reply);
						return reply;
					}
				}
				publishProgress(10000);
				db.setTransactionSuccessful();
			} finally {
				db.setLockingEnabled(true);
				db.endTransaction();
				wordhelp.close();
			}
			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Excution time: " + (endnow - startnow) + " ms");
			Log.d("doInBackground", "line: " + linecount);
			db.close();
			closeStream(dictstream, reply);
			return reply;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			if (pd.isShowing()) {
				pd.setProgress(progress[0]);
			}
		}

		@Override
		protected void onCancelled() {
			// Pointless callback. Thanks android.
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(Reply result) {
			if (pd == null) {
				// Log.d("onPostExecute", "pd");
			} else {
				// Log.d("onPostExecute", "pd");
				if (pd.isShowing()) {
					pd.dismiss();
				}
			}
			if (result == null) {
				// bad thing happened
				Log.e("onPostExecute", "Bad things happen?");
			} else {
				Log.d("onPostExecute", "Result: " + result.status + " " + result.message);
				if (!result.status) {
					showErrorDialog(caller.getResources().getString(R.string.pref_restore_title),
						result.message);
				}
			}
		}
	}

	private class DumpDictTask extends AsyncTask<String, Integer, Reply> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		@Override
		protected Reply doInBackground(String... ignore) {
			Reply reply = new Reply();
			SQLiteDatabase db;
			db = T9DB.getSQLDB(caller);
			long entries;
			int current = 0;
			int pos = 0;
			int last = 0;
			File backupfile = new File(new File(Environment.getExternalStorageDirectory(),
				backupdir), backupname);

			db.setLockingEnabled(false);

			Log.d("doInBakground", "Dumping dict...");
			BufferedWriter bw;
			OutputStream dictstream = null;

			try {
				dictstream = new FileOutputStream(backupfile);
			} catch (FileNotFoundException e) {
				reply.status = false;
				reply.message = "Backup file error: " + e.getMessage();
				db.close();
				closeStream(dictstream, reply); // this is silly but it stops
												// IDE nagging at me.
				return reply;
			}

			try {
				bw = new BufferedWriter(new OutputStreamWriter(dictstream, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				reply.status = false;
				reply.message = "UnsupportedEncodingException Error: " + e.getMessage();
				db.close();
				closeStream(dictstream, reply);
				return reply;
			}
			long startnow, endnow;
			startnow = SystemClock.uptimeMillis();

			String q = "SELECT count(*) FROM " + T9DB.WORD_TABLE_NAME;
			Cursor cur = db.rawQuery(q, null);
			cur.moveToFirst();
			entries = cur.getInt(0);
			// pd.setMax((int)entries);
			cur.close();

			while (current < entries) {
				q = "SELECT " + T9DB.COLUMN_ID + ", " + T9DB.COLUMN_WORD + ", "
					+ T9DB.COLUMN_FREQUENCY + " FROM " + T9DB.WORD_TABLE_NAME + " WHERE "
					+ T9DB.COLUMN_ID + ">" + current + " ORDER BY " + T9DB.COLUMN_ID + " LIMIT "
					+ BACKUP_Q_LIMIT;
				cur = db.rawQuery(q, null);
				for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
					if (isCancelled()) {
						// this is useless because of dumb android bug that
						// doesn't
						// call onCancelled after this method finishes.
						reply.status = false;
						reply.message = "User cancelled.";
						break;
					}
					current = cur.getInt(0);
					pos++;
					try {
						bw.write(cur.getString(1));
						bw.write(" ");
						bw.write(Integer.toString(cur.getInt(2)));
						bw.newLine();
					} catch (IOException e) {
						e.printStackTrace();
						reply.status = false;
						reply.message = "IO Error: " + e.getMessage();
						db.close();
						closeStream(dictstream, reply);
						return reply; // why complain? I closed the stream above
					}
					if ((pos - last) > 80) {
						publishProgress((int) ((float) current / entries * 10000));
						last = current;
					}
				}
				cur.close();
			}
			publishProgress(100);

			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Excution time: " + (endnow - startnow) + " ms");
			Log.d("doInBackground", "entries: " + entries + " last: " + pos);
			db.close();
			try {
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			closeStream(dictstream, reply);
			return reply;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			if (pd.isShowing()) {
				pd.setProgress(progress[0]);
			}
		}

		@Override
		protected void onCancelled() {
			// Pointless callback. Thanks android.
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(Reply result) {
			if (pd == null) {
				// Log.d("onPostExecute", "pd");
			} else {
				// Log.d("onPostExecute", "pd");
				pd.dismiss();
			}
			if (result == null) {
				// bad thing happened
				Log.e("onPostExecute", "Bad things happen?");
			} else {
				Log.d("onPostExecute", "Result: " + result.status + " " + result.message);
				if (!result.status) {
					showErrorDialog(caller.getResources().getString(R.string.pref_backup_title),
						result.message);
				}
			}
		}
	}

	private class NukeDictTask extends AsyncTask<String, Integer, Reply> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		@Override
		protected Reply doInBackground(String... ignore) {
			Reply reply = new Reply();
			Log.d("doInBakground", "Nuking dict...");
			long startnow, endnow;
			startnow = SystemClock.uptimeMillis();
			T9DB t9db = new T9DB(caller);
			t9db.nuke();

			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Excution time: " + (endnow - startnow) + " ms");
			return reply;
		}

		@Override
		protected void onCancelled() {
			// Pointless callback. Thanks android.
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(Reply result) {
			if (pd == null) {
				// Log.d("onPostExecute", "pd");
			} else {
				// Log.d("onPostExecute", "pd");
				pd.dismiss();
			}
			if (result == null) {
				// bad thing happened
				Log.e("onPostExecute", "Bad things happen?");
			} else {
				Log.d("onPostExecute", "Result: " + result.status + " " + result.message);
				if (!result.status) {
					showErrorDialog(caller.getResources().getString(R.string.pref_nuke_title),
						result.message);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		Preference button = getPreferenceManager().findPreference("loaddict");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					loadDict();
					return true;
				}
			});
		}
		button = getPreferenceManager().findPreference("nukedict");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					nukeDict();
					return true;
				}
			});
		}

		button = getPreferenceManager().findPreference("backupdict");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					backupDict();
					return true;
				}
			});
		}

		button = getPreferenceManager().findPreference("restoredict");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					restoreDict();
					return true;
				}
			});
		}

		button = getPreferenceManager().findPreference("querytest");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					queryTestSingle();
					return true;
				}
			});
		}

		caller = this;
	}

	private void loadDict() {
		preloader(R.string.pref_loadingdict, "");
	}

	private void preloader(int msgid, String mode) {
		pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(msgid));
		pd.setOnCancelListener(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMax(10000);
		pd.show();
		task = new LoadDictTask();
		task.execute(mode);
	}

	private void predumper(int msgid) {
		pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(msgid));
		pd.setOnCancelListener(this);
		// pd.setProgressNumberFormat(null); Why added in API11...
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMax(10000);
		pd.show();
		task = new DumpDictTask();
		task.execute("");
	}

	private void prenuke(int msgid) {
		pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(msgid));
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.show();
		task = new NukeDictTask();
		task.execute("");
	}

	private void nukeDict() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.pref_nuke_warn).setTitle(R.string.pref_nuke_title)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					prenuke(R.string.pref_nukingdict);
				}
			}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void backupDict() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			File saveloc = new File(Environment.getExternalStorageDirectory(), backupdir);
			saveloc.mkdirs();
			if (!saveloc.canWrite()) {
				Log.e("backupDict", "can't write : " + saveloc.getAbsolutePath());
				showErrorDialogID(builder, R.string.pref_backup_title, R.string.pref_backup_noext);
				return;
			}

			saveloc = new File(saveloc, backupname);
			if (saveloc.exists()) {
				builder.setMessage(R.string.pref_backup_warn).setTitle(R.string.pref_backup_title)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							predumper(R.string.pref_savingbackup);
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				predumper(R.string.pref_savingbackup);
			}
		} else {
			showErrorDialogID(builder, R.string.pref_backup_title, R.string.pref_backup_noext);
		}
	}

	private void showErrorDialog(String title, String msg) {
		showErrorDialog(new AlertDialog.Builder(this), title, msg);
	}

	private void showErrorDialog(AlertDialog.Builder builder, String title, String msg) {
		builder.setMessage(msg).setTitle(title)
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void showErrorDialogID(AlertDialog.Builder builder, int titleid, int msgid) {
		builder.setMessage(msgid).setTitle(titleid)
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void restoreDict() {
		// Environment.MEDIA_MOUNTED_READ_ONLY;
		// Environment.MEDIA_MOUNTED;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())
			|| Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			if ((new File(new File(Environment.getExternalStorageDirectory(), backupdir),
				backupname)).exists()) {
				Resources res = getResources();
				builder
					.setMessage(
						res.getString(R.string.pref_restore_warn,
							res.getString(R.string.pref_nukedict)))
					.setTitle(R.string.pref_restore_title)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							preloader(R.string.pref_loadingbackup, "backup");
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				showErrorDialogID(builder, R.string.pref_restore_title,
					R.string.pref_restore_nofile);
			}
		} else {
			showErrorDialogID(builder, R.string.pref_restore_title, R.string.pref_restore_noext);
		}
	}

	@SuppressWarnings("unused")
	private void queryTestDebug() {
		long startnow, endnow;
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<Integer> ids = new ArrayList<Integer>();

		startnow = SystemClock.uptimeMillis();

		T9DB tdb = new T9DB(this);
		tdb.init();
		Log.d("queryTestDebug", "Testing...");
		tdb.updateWords("123", words, ids, 0);
		Log.d("queryTestDebug", "123->" + words.toString());
		Log.d("queryTestDebug", "269->");
		tdb.updateWords("269", words, ids, 0);
		Iterator<String> i = words.iterator();
		while (i.hasNext()) {
			Log.d("queryTestDebug", "word: " + i.next());
		}

		Log.d("queryTestDebug", "228->");
		tdb.updateWords("228", words, ids, 0);
		i = words.iterator();
		while (i.hasNext()) {
			Log.d("queryTestDebug", "word: " + i.next());
		}
		endnow = SystemClock.uptimeMillis();
		Log.d("TIMING", "Excution time: " + (endnow - startnow) + " ms");
		tdb.close();
	}

	@SuppressWarnings("unused")
	private void queryTest() {
		long startnow, endnow;
		startnow = SystemClock.uptimeMillis();

		T9DB tdb = new T9DB(this);
		tdb.init();

		// tdb.getWords("123").iterator();
		// tdb.getWords("269").iterator();
		// tdb.getWords("228").iterator();
		// tdb.getWords("24371").iterator();
		// tdb.getWords("22376284").iterator();
		// tdb.getWords("68372667367283").iterator();
		// tdb.getWords("22637").iterator();

		endnow = SystemClock.uptimeMillis();
		Log.d("TIMING", "Excution time: " + (endnow - startnow) + " ms");
		tdb.close();
	}

	private void queryTestSingle() {
		long startnow, endnow;
		int size;
		ArrayList<String> words = new ArrayList<String>(8);
		ArrayList<Integer> ids = new ArrayList<Integer>(8);
		startnow = SystemClock.uptimeMillis();

		T9DB tdb = new T9DB(this);
		tdb.init();

		tdb.updateWords("222", words, ids, 0);
		size = ids.size();
		if (size > 0) {
			tdb.incrementWord(ids.get(0));
			tdb.incrementWord(ids.get(0));
			tdb.incrementWord(ids.get(0));
		}

		for (int x = 0; x < size; x++) {
			tdb.incrementWord(ids.get(x));
		}

		endnow = SystemClock.uptimeMillis();
		Log.d("TIMING", "Excution time: " + (endnow - startnow) + " ms");

		ArrayList<Integer> freqs = new ArrayList<Integer>(8);
		tdb.updateWordsW("222", words, ids, freqs);
		Log.d("VALUES", "...");
		size = freqs.size();
		for (int x = 0; x < size; x++) {
			Log.d("VALUES",
				"Word: " + words.get(x) + " id: " + ids.get(x) + " freq: " + freqs.get(x));
		}
		Log.d("queryTestSingle", "done.");
		tdb.close();
	}

	@Override
	public void onCancel(DialogInterface dint) {
		task.cancel(false);
	}
}
