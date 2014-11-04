package org.nyanya.android.traditionalt9;

import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class KeyMap {
	public static final String keymapfname = "keymap.txt";

	public static int DPAD_CENTER = KeyEvent.KEYCODE_DPAD_CENTER;
	public static int DPAD_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	public static int DPAD_UP = KeyEvent.KEYCODE_DPAD_UP;
	public static int DPAD_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	public static int DPAD_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	public static int SOFT_RIGHT = KeyEvent.KEYCODE_SOFT_RIGHT;
	public static int SOFT_LEFT = KeyEvent.KEYCODE_SOFT_LEFT;
	public static int DEL = KeyEvent.KEYCODE_DEL;
	public static int BACK = KeyEvent.KEYCODE_BACK;
	public static int ENTER = KeyEvent.KEYCODE_ENTER;

	static {
		setKeys();
	}

	public static void setKeys() {
		// check storage
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())
				|| Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// check for file
			if ((new File(new File(Environment.getExternalStorageDirectory(), TraditionalT9Settings.sddir),
					keymapfname)).exists()) {
				BufferedReader br = null;
				Log.d("KeyMap", "Attemping to load keys");
				try {
					br = new BufferedReader(new FileReader(new File(
							new File(Environment.getExternalStorageDirectory(), TraditionalT9Settings.sddir), keymapfname)));
					String line;
					try {
						while ((line = br.readLine()) != null) {
							String[] ws = line.split(" ");
							if (ws.length != 2) {continue;}
							try {
								if (ws[0].equals("DPAD_CENTER")) {
									DPAD_CENTER = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("DPAD_DOWN")) {
									DPAD_DOWN = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("DPAD_UP")) {
									DPAD_UP = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("DPAD_LEFT")) {
									DPAD_LEFT = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("DPAD_RIGHT")) {
									DPAD_RIGHT = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("SOFT_RIGHT")) {
									SOFT_RIGHT = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("SOFT_LEFT")) {
									SOFT_LEFT = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("DEL")) {
									DEL = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("BACK")) {
									BACK = Integer.parseInt(ws[1]);
								} else if (ws[0].equals("ENTER")) {
									ENTER = Integer.parseInt(ws[1]);
								}
							} catch (NumberFormatException _ignore) {
								Log.w("T9.KeyMap", "Invalid number found");
							}
						}
						Log.d("KeyMap", "Done.");
					} catch (IOException _ignore) {
						Log.e("T9.KeyMap", "Error while reading line.");
						try { br.close(); }
						catch (IOException ignored) {}
					}
				} catch (FileNotFoundException ignored) {	}
			}
		}
	}
}
