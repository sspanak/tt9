package org.nyanya.android.traditionalt9;

/*
	Source for English dictionary: http://wordlist.sourceforge.net/
	Source for Russian dictionary: Various sources from Russian user
 */

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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.stackoverflow.answer.UnicodeBOMInputStream;

import pl.wavesoftware.widget.MultiSelectListPreference;

public class TraditionalT9Settings extends PreferenceActivity implements
		DialogInterface.OnCancelListener {

	AsyncTask<String, Integer, Reply> task = null;
	final static String dictname = "%s-utf8.txt";
	final static String userdictname = "user.%s.dict";
	final static String backupname = "t9backup.txt";
	final static String sddir = "traditionalt9";

	final int BACKUP_Q_LIMIT = 1000;

	Context mContext = null;

	public class LoadException extends Exception {
		private static final long serialVersionUID = 3323913652550046354L;

		public LoadException() {
			super();
		}
	}

	private class Reply {
		public boolean status;
		private List<String> msgs;

		protected Reply() {
			this.status = true;
			this.msgs = new LinkedList<String>();
		}

		protected void addMsg(String msg) throws LoadException {
			msgs.add(msg);
			if (msgs.size() > 6) {
				msgs.add("Too many errors, bailing.");
				throw new LoadException();
			}
		}
		protected void forceMsg(String msg) {
			msgs.add(msg);
		}

	}

	private void finishAndShowError(ProgressDialog pd, Reply result, int title){
		if (pd != null) {
			// Log.d("onPostExecute", "pd");
			if (pd.isShowing()) {
				pd.dismiss();
			}
		}
		if (result == null) {
			// bad thing happened
			Log.e("onPostExecute", "Bad things happen?");
		} else {
			String msg = TextUtils.join("\n", result.msgs);
			Log.d("onPostExecute", "Result: " + result.status + " " + msg);
			if (!result.status) {
				showErrorDialog(getResources().getString(title), msg);
			}
		}
	}

	private void closeStream(Closeable is, Reply reply) {
		if (is == null) {
			return;
		}
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			reply.forceMsg("Couldn't close stream: " + e.getMessage());
		}
	}

	private class LoadDictTask extends AsyncTask<String, Integer, Reply> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		ProgressDialog pd;
		long size;
		long pos;
		boolean internal;
		boolean restore;
		String[] dicts;
		int[] mSupportedLanguages;

		LoadDictTask(int msgid, boolean intern, boolean restorebackup, int[] supportedLanguages) {
			internal = intern;
			restore = restorebackup;

			int suplanglen = supportedLanguages.length;
			dicts = new String[suplanglen];
			for (int x=0; x<suplanglen; x++) {
				if (intern) {
					dicts[x] = String.format(dictname,
							LangHelper.LANGS[supportedLanguages[x]].toLowerCase(Locale.ENGLISH));
				} else {
					dicts[x] = String.format(userdictname,
							LangHelper.LANGS[supportedLanguages[x]].toLowerCase(Locale.ENGLISH));
				}
			}
			mSupportedLanguages = supportedLanguages;

			pd = new ProgressDialog(TraditionalT9Settings.this);
			pd.setMessage(getResources().getString(msgid));
			pd.setOnCancelListener(TraditionalT9Settings.this);
		}

		private long getDictSizes(boolean internal, boolean restore, String[] dicts) {
			if (internal) {
				InputStream input;
				Properties props = new Properties();
				try {
					input = getAssets().open("dict.properties");
					props.load(input);
					long total = 0;
					for (String dict : dicts) {
						total += Long.parseLong(props.getProperty("size." + dict));
					}
					return total;

				} catch (IOException e) {
					Log.e("getDictSizes", "Unable to get dict sizes");
					e.printStackTrace();
					return -1;
				} catch (NumberFormatException e) {
					Log.e("getDictSizes", "Unable to parse sizes");
					return -1;
				}
			} else {
				File backupfile = new File(Environment.getExternalStorageDirectory(), sddir);
				if (restore) {
					// using external backup
					backupfile = new File(backupfile, backupname);
					if (backupfile.exists() && backupfile.isFile()) {
						return backupfile.length();
					} else {
						return -1;
					}
				} else {
					long total = 0;
					File f;
					for (String dict : dicts) {
						f = new File(backupfile, dict);
						if (f.exists() && f.isFile()) {
							total = total + f.length();
						} else {
							total = total + 0;
						}
					}
					return total;
				}
			}
		}

		@Override protected void onPreExecute() {
			size = getDictSizes(internal, restore, dicts);
			pos = 0;
			if ( size >= 0 ) {
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pd.setMax(10000);
			} else {
				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			}
			pd.show();
		}

		@Override
		protected Reply doInBackground(String... mode) {
			Reply reply = new Reply();
			SQLiteDatabase db;
			db = T9DB.getSQLDB(mContext);
			if (db == null) {
				reply.forceMsg("Database unavailable at this time. (May be updating)");
				reply.status = false;
				return reply;
			}
			db.setLockingEnabled(false);

			long startnow, endnow;
			startnow = SystemClock.uptimeMillis();

			// add characters first, then dictionary:
			Log.d("doInBackground", "Adding characters...");
			// load characters from supported langs
			for (int lang : mSupportedLanguages) {
				processChars(db, lang);
			}
			Log.d("doInBackground", "done.");

			Log.d("doInBackground", "Adding dict(s)...");

			InputStream dictstream = null;

			try {
				if (restore) {
					try {
						dictstream = new FileInputStream(new File(
								new File(Environment.getExternalStorageDirectory(), sddir),	backupname));
						reply = processFile(dictstream, reply, db, -1, backupname);
					} catch (FileNotFoundException e) {
						reply.status = false;
						reply.forceMsg("Backup file not found: " + e.getMessage());
						closeStream(dictstream, reply); // this is silly but it
						return reply;					// stops IDE nagging at me.
					} catch (IOException e) {
						reply.status = false;
						reply.forceMsg("IO Error: " + e.getMessage());
						closeStream(dictstream, reply); // this is silly but it
						return reply;					// stops IDE nagging at me.
					}
					closeStream(dictstream, reply);
				} else {
					for (int x=0; x<dicts.length; x++) {
						if (internal) {
							try {
								dictstream = getAssets().open(dicts[x]);
								reply = processFile(dictstream, reply, db, mSupportedLanguages[x], dicts[x]);
							} catch (IOException e) {
								e.printStackTrace();
								reply.status = false;
								reply.forceMsg("IO Error: " + e.getMessage());
							}
						} else {
							try {
								dictstream = new FileInputStream(new File(
										new File(Environment.getExternalStorageDirectory(), sddir),	dicts[x]));
								reply = processFile(dictstream, reply, db, mSupportedLanguages[x], dicts[x]);
							} catch (FileNotFoundException e) {
								reply.status = false;
								reply.forceMsg("File not found: " + e.getMessage());
								final String msg = mContext.getString(R.string.pref_loaduser_notfound, dicts[x]);
								//Log.d("T9Setting.load", "Built string. Calling Toast.");
								((Activity) mContext).runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(mContext,
												msg,
												Toast.LENGTH_SHORT).show();
									}
								});

								closeStream(dictstream, reply); // this is silly but it
								// stops IDE nagging at me.
							} catch (IOException e) {
								reply.status = false;
								reply.forceMsg("IO Error: " + e.getMessage());
								closeStream(dictstream, reply); // this is silly but it
								return reply;					// stops IDE nagging at me.
							}
						}
						closeStream(dictstream, reply);
					}
				}
			} catch (LoadException e) {
				// too many errors, bail
				closeStream(dictstream, reply);
			}
			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");
			return reply;
		}

		private void processChars(SQLiteDatabase db, int lang) {
			InsertHelper wordhelp = new InsertHelper(db, T9DB.WORD_TABLE_NAME);

			final int wordColumn = wordhelp.getColumnIndex(T9DB.COLUMN_WORD);
			final int langColumn = wordhelp.getColumnIndex(T9DB.COLUMN_LANG);
			final int freqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_FREQUENCY);
			final int seqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_SEQ);

			for (Map.Entry<Character, Integer> entry : CharMap.CHARTABLE.get(lang).entrySet()) {
				wordhelp.prepareForReplace();
				wordhelp.bind(langColumn, Integer.toString(lang));
				wordhelp.bind(seqColumn, Integer.toString(entry.getValue()));
				wordhelp.bind(wordColumn, Character.toString(entry.getKey()));
				wordhelp.bind(freqColumn, 0);
				wordhelp.execute();
			}
		}

		private String getLine(BufferedReader br, Reply rpl, String fname) throws LoadException {
			try {
				return br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				rpl.status = false;
				rpl.addMsg("IO Error ("+fname+"): " + e.getMessage());
			}
			return null;
		}

		private Reply processFile(InputStream is, Reply rpl, SQLiteDatabase db, int lang, String fname)
				throws LoadException, IOException {
			long last = 0;
			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(is);

			BufferedReader br = new BufferedReader(new InputStreamReader(ubis));
			ubis.skipBOM();

			InsertHelper wordhelp = new InsertHelper(db, T9DB.WORD_TABLE_NAME);
			final int langColumn = wordhelp.getColumnIndex(T9DB.COLUMN_LANG);
			final int wordColumn = wordhelp.getColumnIndex(T9DB.COLUMN_WORD);
			final int freqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_FREQUENCY);
			final int seqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_SEQ);

			String[] ws;
			int freq;
			String seq;
			int linecount = 1;
			int wordlen;
			String word = getLine(br, rpl, fname);

			db.beginTransaction();

			try {
				while (word != null) {
					if (isCancelled()) {
						rpl.status = false;
						rpl.addMsg("User cancelled.");
						break;
					}
					if (word.contains(" ")) {
						ws = word.split(" ");
						word = ws[0];
						try {
							freq = Integer.parseInt(ws[1]);
						} catch (NumberFormatException e) {
							rpl.status = false;
							rpl.addMsg("Number error ("+fname+") at line " + linecount+". Using 0 for frequency.");
							freq = 0;
						}
						if (lang == -1 && ws.length == 3) {
							try {
								lang = Integer.parseInt(ws[2]);
							} catch (NumberFormatException e) {
								rpl.status = false;
								rpl.addMsg("Number error ("+fname+") at line " + linecount+". Using 0 (en) for language.");
								lang = 0;
							}
							if (lang >= LangHelper.LANGS.length) {
								rpl.status = false;
								rpl.addMsg("Unsupported language ("+fname+") at line " + linecount+". Trying 0 (en) for language.");
								lang = 0;
							}
						} else if (lang == -1) {
							lang = 0;
						}
					} else {
						freq = 0;
					}

					try {
						wordlen = word.getBytes("UTF-8").length;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						rpl.status = false;
						rpl.addMsg("Encoding Error("+fname+") line "+linecount+": " + e.getMessage());
						wordlen = word.length();
					}
					pos += wordlen;
					// replace junk characters:
					word = word.replace("\uFEFF", "");
					try {
						seq = CharMap.getStringSequence(word, lang);
					} catch (NullPointerException e) {
						rpl.status = false;
						rpl.addMsg("Error on word ("+word+") line "+
								linecount+" in (" +	fname+"): "+
								getResources().getString(R.string.add_word_badchar, LangHelper.LANGS[lang]));
						break;
					}
					linecount++;
					wordhelp.prepareForReplace();
					wordhelp.bind(seqColumn, seq);
					wordhelp.bind(langColumn, lang);
					wordhelp.bind(wordColumn, word);
					wordhelp.bind(freqColumn, freq);
					wordhelp.execute();

					// System.out.println("Progress: " + pos + " Last: " + last
					// + " fsize: " + fsize);
					if ((pos - last) > 4096) {
						// Log.d("doInBackground", "line: " + linecount);
						// Log.d("doInBackground", "word: " + word);
						publishProgress((int) ((float) pos / size * 10000));
						last = pos;
					}
					word = getLine(br, rpl, fname);
				}
				publishProgress(10000);
				db.setTransactionSuccessful();
			} finally {
				db.setLockingEnabled(true);
				db.endTransaction();
				br.close();
				is.close();
				ubis.close();
				is.close();
				wordhelp.close();
			}
			return rpl;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			if (pd.isShowing()) {
				pd.setProgress(progress[0]);
			}
		}

		@Override
		protected void onPostExecute(Reply result) {
			if (restore) {
				finishAndShowError(pd, result, R.string.pref_restore_title);
			} else {
				finishAndShowError(pd, result, R.string.pref_load_title);
			}
		}
	}

	private class DumpDictTask extends AsyncTask<String, Integer, Reply> {

		ProgressDialog pd;

		DumpDictTask(int msgid) {
			pd = new ProgressDialog(TraditionalT9Settings.this);
			pd.setMessage(getResources().getString(msgid));
			pd.setOnCancelListener(TraditionalT9Settings.this);
		}

		@Override protected void onPreExecute() {
			pd.show();
		}

		@Override
		protected Reply doInBackground(String... ignore) {
			Reply reply = new Reply();
			SQLiteDatabase db;
			db = T9DB.getSQLDB(mContext);
			if (db == null) {
				reply.forceMsg("Database unavailable at this time. (May be updating)");
				reply.status = false;
				return reply;
			}
			long entries;
			int current = 0;
			int pos = 0;
			int last = 0;
			File backupfile = new File(new File(Environment.getExternalStorageDirectory(),
					sddir), backupname);

			db.setLockingEnabled(false);

			Log.d("doInBackground", "Dumping dict...");
			BufferedWriter bw;
			OutputStream dictstream = null;

			try {
				dictstream = new FileOutputStream(backupfile);
			} catch (FileNotFoundException e) {
				reply.status = false;
				reply.forceMsg("Backup file error: " + e.getMessage());
				return reply;
			}

			try {
				bw = new BufferedWriter(new OutputStreamWriter(dictstream, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				reply.status = false;
				reply.forceMsg("Encoding Error (backupfile): " + e.getMessage());
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
			try {
				while (pos < entries) {
					q = "SELECT " + T9DB.COLUMN_ID + ", " + T9DB.COLUMN_LANG + ", " +
							T9DB.COLUMN_WORD + ", " + T9DB.COLUMN_FREQUENCY +
							" FROM " + T9DB.WORD_TABLE_NAME +
							" WHERE " + T9DB.COLUMN_ID + ">" + current +
							" ORDER BY " + T9DB.COLUMN_ID +	" LIMIT " + BACKUP_Q_LIMIT;
					cur = db.rawQuery(q, null);
					for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
						if (isCancelled()) {
							reply.status = false;
							reply.forceMsg("User cancelled.");
							break;
						}
						current = cur.getInt(0);
						pos++;
						try {
							bw.write(cur.getString(2)); // write word
							bw.write(" ");
							bw.write(Integer.toString(cur.getInt(3))); // then freq
							bw.write(" ");
							bw.write(Integer.toString(cur.getInt(1))); // then lang
							bw.newLine();
						} catch (Exception e) {
							e.printStackTrace();
							reply.status = false;
							reply.forceMsg("Error: " + e.getMessage());
							closeStream(dictstream, reply);
						}
						if ((pos - last) > 80) {
							publishProgress((int) ((float) pos / entries * 10000));
							last = pos;
						}
					}
					cur.close();
				}
			} finally {

			}
			publishProgress(10000);

			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");
			Log.d("doInBackground", "entries: " + entries + " last: " + pos);
			try {
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bw.close();
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

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(Reply result) {
			finishAndShowError(pd, result, R.string.pref_backup_title);
		}
	}

	private class NukeDictTask extends AsyncTask<String, Integer, Reply> {

		ProgressDialog pd;

		NukeDictTask(int msgid) {
			pd = new ProgressDialog(TraditionalT9Settings.this);
			pd.setMessage(getResources().getString(msgid));
			pd.setCancelable(false);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		}

		@Override protected void onPreExecute() {
			pd.show();
		}

		@Override
		protected Reply doInBackground(String... ignore) {
			Reply reply = new Reply();
			Log.d("doInBackground", "Nuking dict...");
			long startnow, endnow;
			startnow = SystemClock.uptimeMillis();
			T9DB t9db = T9DB.getInstance(mContext);
			t9db.nuke();

			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");
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
			finishAndShowError(pd, result, R.string.pref_nuke_title);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		Preference button = getPreferenceManager().findPreference("help");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					openHelp();
					return true;
				}
			});
		}

		button = getPreferenceManager().findPreference("loaddict");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					preloader(R.string.pref_loadingdict, true, false);
					return true;
				}
			});
		}

		button = getPreferenceManager().findPreference("loaduserdict");
		if (button != null) {
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					preloader(R.string.pref_loadinguserdict, false, false);
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
					test();
					return true;
				}
			});
		}

		mContext = this;
	}

	private void openHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(getString(R.string.help_url)));
		startActivity(i);
	}

	// LoadDictTask(int msgid, boolean intern, boolean restorebackup, CheckBoxPreference setting)
	private void preloader(int msgid, boolean internal, boolean restorebackup) {

		task = new LoadDictTask(msgid, internal, restorebackup,
				LangHelper.buildLangs(((ListPreference) findPreference("pref_lang_support")).getValue()));
		task.execute();
	}

	private void predumper(int msgid) {
		task = new DumpDictTask(msgid);
		task.execute();
	}

	private void nukeDict() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.pref_nuke_warn).setTitle(R.string.pref_nuke_title)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						task = new NukeDictTask(R.string.pref_nukingdict);
						task.execute();
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

			File saveloc = new File(Environment.getExternalStorageDirectory(), sddir);
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

	private void showErrorDialog(CharSequence title, CharSequence msg) {
		showErrorDialog(new AlertDialog.Builder(this), title, msg);
	}

	private void showErrorDialog(AlertDialog.Builder builder, CharSequence title, CharSequence msg) {
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
			if ((new File(new File(Environment.getExternalStorageDirectory(), sddir),
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
								preloader(R.string.pref_loadingbackup, false, true);
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
		AbstractList<String> words = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		startnow = SystemClock.uptimeMillis();

		T9DB tdb = T9DB.getInstance(this);
		Log.d("queryTestDebug", "Testing...");
		tdb.updateWords("123", words, ids, 0, LangHelper.EN);
		Log.d("queryTestDebug", "123->" + words.toString());
		Log.d("queryTestDebug", "269->");
		tdb.updateWords("269", words, ids, 0, LangHelper.EN);
		Iterator<String> i = words.iterator();
		while (i.hasNext()) {
			Log.d("queryTestDebug", "word: " + i.next());
		}

		Log.d("queryTestDebug", "228->");
		tdb.updateWords("228", words, ids, 0, LangHelper.EN);
		i = words.iterator();
		while (i.hasNext()) {
			Log.d("queryTestDebug", "word: " + i.next());
		}
		endnow = SystemClock.uptimeMillis();
		Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");
	}

	@SuppressWarnings("unused")
	private void queryTest() {
		long startnow, endnow;
		startnow = SystemClock.uptimeMillis();

		T9DB tdb = T9DB.getInstance(this);

		// tdb.getWords("123").iterator();
		// tdb.getWords("269").iterator();
		// tdb.getWords("228").iterator();
		// tdb.getWords("24371").iterator();
		// tdb.getWords("22376284").iterator();
		// tdb.getWords("68372667367283").iterator();
		// tdb.getWords("22637").iterator();

		endnow = SystemClock.uptimeMillis();
		Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");
	}

	@SuppressWarnings("unused")
	private void queryTestSingle() {
		long startnow, endnow;
		int size;
		AbstractList<String> words = new ArrayList<String>(8);
		ArrayList<Integer> ids = new ArrayList<Integer>(8);
		startnow = SystemClock.uptimeMillis();

		T9DB tdb = T9DB.getInstance(this);

		tdb.updateWords("222", words, ids, 0, LangHelper.EN);
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
		Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");

		List<Integer> freqs = new ArrayList<Integer>(8);
		tdb.updateWordsW("222", words, ids, freqs, LangHelper.EN);
		Log.d("VALUES", "...");
		size = freqs.size();
		for (int x = 0; x < size; x++) {
			Log.d("VALUES",
					"Word: " + words.get(x) + " id: " + ids.get(x) + " freq: " + freqs.get(x));
		}
		Log.d("queryTestSingle", "done.");
	}
	private void test() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if ( "".equalsIgnoreCase(pref.getString("pref_lang_support", null)) ) {
			Log.d("T9Settings.test", "AAAAAAAAAA blank string");
		}
		Log.d("T9Settings.test", pref.getString("pref_lang_support", "aaaaaaaaaa"));
	}
	@Override
	public void onCancel(DialogInterface dint) {
		task.cancel(false);
	}
}
