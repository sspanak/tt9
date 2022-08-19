package io.github.sspanak.tt9.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.stackoverflow.answer.UnicodeBOMInputStream;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.T9DB;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.T9Preferences;
import io.github.sspanak.tt9.settings.CustomInflater;
import io.github.sspanak.tt9.settings.Setting;
import io.github.sspanak.tt9.settings.SettingAdapter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class TraditionalT9Settings extends ListActivity implements DialogInterface.OnCancelListener {

	AsyncTask<String, Integer, Reply> task = null;
	final static String userdictname = "user.%s.dict";
	final static String sddir = "tt9";

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
			this.msgs = new ArrayList<String>(4);
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

	private static void closeStream(Closeable is, Reply reply) {
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
		String[] dicts;
		ArrayList<Language> mSupportedLanguages;

		LoadDictTask(int msgid, boolean intern, ArrayList<Language> supportedLanguages) {
			internal = intern;

			dicts = new String[supportedLanguages.size()];
			int x = 0;
			for (Language language : supportedLanguages) {
				if (intern) {
					dicts[x++] = language.getDictionaryFile();
				} else {
					dicts[x++] = String.format(userdictname, language.getName().toLowerCase(Locale.ENGLISH));
				}
			}
			mSupportedLanguages = supportedLanguages;

			pd = new ProgressDialog(TraditionalT9Settings.this);
			pd.setMessage(getResources().getString(msgid));
			pd.setOnCancelListener(TraditionalT9Settings.this);
		}

		private long getDictSizes(boolean internal, String[] dicts) {
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

		@Override protected void onPreExecute() {
			size = getDictSizes(internal, dicts);
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
			for (Language lang : mSupportedLanguages) {
				processChars(db, lang);
			}
			Log.d("doInBackground", "done.");

			Log.d("doInBackground", "Adding dict(s)...");

			InputStream dictstream = null;

			try {
				for (int x=0; x<dicts.length; x++) {
					if (internal) {
						try {
							dictstream = getAssets().open(dicts[x]);
							reply = processFile(dictstream, reply, db, mSupportedLanguages.get(x), dicts[x]);
						} catch (IOException e) {
							e.printStackTrace();
							reply.status = false;
							reply.forceMsg("IO Error: " + e.getMessage());
						}
					} else {
						try {
							dictstream = new FileInputStream(new File(
									new File(Environment.getExternalStorageDirectory(), sddir),	dicts[x]));
							reply = processFile(dictstream, reply, db, mSupportedLanguages.get(x), dicts[x]);
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
			} catch (LoadException e) {
				// too many errors, bail
				closeStream(dictstream, reply);
			}
			endnow = SystemClock.uptimeMillis();
			Log.d("TIMING", "Execution time: " + (endnow - startnow) + " ms");
			return reply;
		}

		/**
		 * processChars
		 * Inserts single characters.
		 */
		private void processChars(SQLiteDatabase db, Language lang) {
			InsertHelper wordhelp = new InsertHelper(db, T9DB.WORD_TABLE_NAME);

			final int wordColumn = wordhelp.getColumnIndex(T9DB.COLUMN_WORD);
			final int langColumn = wordhelp.getColumnIndex(T9DB.COLUMN_LANG);
			final int freqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_FREQUENCY);
			final int seqColumn = wordhelp.getColumnIndex(T9DB.COLUMN_SEQ);

			try {
				for (int key = 0; key <= 9; key++) {
					for (int lowercase = 0; lowercase < 2; lowercase++) {
						for (String langChar : lang.getKeyCharacters(key, lowercase == 0)) {
							wordhelp.prepareForReplace();
							wordhelp.bind(langColumn, lang.getId());
							wordhelp.bind(seqColumn, key);
							wordhelp.bind(wordColumn, langChar);
							wordhelp.bind(freqColumn, 0);
							wordhelp.execute();
						}
					}
				}
			} finally {
				wordhelp.close();
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

		private Reply processFile(InputStream is, Reply rpl, SQLiteDatabase db, Language lang, String fname)
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
						rpl.status = false;
						rpl.addMsg("Cannot parse word with spaces: " + word);
						break;
					}

					freq = 0;
					wordlen = word.getBytes(StandardCharsets.UTF_8).length;
					pos += wordlen;
					// replace junk characters:
					word = word.replace("\uFEFF", "");
					try {
						seq = lang.getDigitSequenceForWord(word);
					} catch (Exception e) {
						rpl.status = false;
						rpl.addMsg("Error on word ("+word+") line "+
								linecount+" in (" +	fname+"): "+
								getResources().getString(R.string.add_word_badchar, lang.getName(), word));
						break;
					}
					linecount++;
					wordhelp.prepareForReplace();
					wordhelp.bind(seqColumn, seq);
					wordhelp.bind(langColumn, lang.getId());
					wordhelp.bind(wordColumn, word);
					wordhelp.bind(freqColumn, freq);
					wordhelp.execute();

					// System.out.println("Progress: " + pos + " Last: " + last
					// + " fsize: " + fsize);
					if ((pos - last) > 4096) {
						// Log.d("doInBackground", "line: " + linecount);
						// Log.d("doInBackground", "word: " + word);
						if (size >= 0) { publishProgress((int) ((float) pos / size * 10000)); }
						last = pos;
					}
					word = getLine(br, rpl, fname);
				}
				publishProgress((int) ((float) pos / size * 10000));
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
			finishAndShowError(pd, result, R.string.pref_load_title);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// maybe need this?
		// http://stackoverflow.com/questions/7645880/listview-with-onitemclicklistener-android

		// get settings
		T9Preferences prefs = new T9Preferences(this);
		Object[] settings = {
			prefs.getInputMode(),
			prefs.getEnabledLanguageRaw(),
			null, // MODE_NOTIFY; not used, remove in #29
			false, // KEY_REMAP; not used, remove in #29
			true, // SPACE_ZERO; not used, remove in #29
		};
		ListAdapter settingitems;
		try {
			settingitems = new SettingAdapter(this, CustomInflater.inflate(this, R.xml.prefs, settings));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		setContentView(R.layout.preference_list_content);
		setListAdapter(settingitems);
		mContext = this;

	}

	@Override
	protected  void onListItemClick(ListView l, View v, int position, long id) {
		Setting s = (Setting)getListView().getItemAtPosition(position);
		if (s.id.equals("help"))
			openHelp();
		else if (s.id.equals("loaddict"))
			preloader(R.string.pref_loadingdict, true);
		else if (s.id.equals("truncatedict")) {
			T9DB.getInstance(this).truncate();
		}
		else if (s.id.equals("loaduserdict"))
			preloader(R.string.pref_loadinguserdict, false);
		else
			s.clicked(mContext);
	}

	private void openHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(getString(R.string.help_url)));
		startActivity(i);
	}

	private void preloader(int msgid, boolean internal) {


		task = new LoadDictTask(
			msgid,
			internal,
			LanguageCollection.getAll(T9Preferences.getInstance(mContext).getEnabledLanguageIds())
		);
		task.execute();
	}


	private void showErrorDialog(CharSequence title, CharSequence msg) {
		showErrorDialog(new AlertDialog.Builder(this), title, msg);
	}

	private void showErrorDialog(AlertDialog.Builder builder, CharSequence title, CharSequence msg) {
		builder.setMessage(msg).setTitle(title)
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	@Override
	public void onCancel(DialogInterface dint) {
		task.cancel(false);
	}
}
