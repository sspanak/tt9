package org.nyanya.android.traditionalt9;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KeyMap {
	public static final String keymapfname = "keymap.txt";
	public static Map<Integer, Integer> keyMapping = new HashMap<Integer, Integer>();

	static {
		setKeys();
	}

	public static int setKeys() {
		int msg = 0;
		keyMapping = new HashMap<Integer, Integer>();
		// check storage
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())
				|| Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// check for file
			if ((new File(new File(Environment.getExternalStorageDirectory(), TraditionalT9Settings.sddir),
					keymapfname)).exists()) {
				BufferedReader br = null;
				Log.d("T9.KeyMap", "Attemping to load keys");
				try {
					br = new BufferedReader(new FileReader(new File(
							new File(Environment.getExternalStorageDirectory(), TraditionalT9Settings.sddir), keymapfname)));
					String line;
					try {
						while ((line = br.readLine()) != null) {
							String[] ws = line.split(" ");
							if (ws.length != 2) {continue;}
							else if (line.startsWith("#")) {continue;}
							try {
								keyMapping.put(Integer.parseInt(ws[0]), Integer.parseInt(ws[1]));
							} catch (NumberFormatException _ignore) {
								Log.w("T9.KeyMap", "Invalid number found");
								msg = R.string.pref_reloadKeysDoneWE;
							}
						}
						Log.d("T9.KeyMap", "Done.");
					} catch (IOException _ignore) {
						Log.e("T9.KeyMap", "Error while reading line.");
						try { br.close(); }
						catch (IOException ignored) {}
					}
				} catch (FileNotFoundException ignored) { msg = R.string.pref_reloadKeysDone; }
			} else { msg = R.string.pref_reloadKeysNoFile; }
		} else { msg = R.string.pref_reloadKeysNoFile; }
		return msg;
	}
}
