package io.github.sspanak.tt9.ui;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class UI {
	public static void showAddWordDialog(TraditionalT9 tt9, int language, String currentWord) {
		Intent awIntent = new Intent(tt9, AddWordAct.class);
		awIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		awIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		awIntent.putExtra("io.github.sspanak.tt9.word", currentWord);
		awIntent.putExtra("io.github.sspanak.tt9.lang", language);
		tt9.startActivity(awIntent);
	}


	public static void showSettingsScreen(TraditionalT9 tt9) {
		Intent prefIntent = new Intent(tt9, PreferencesActivity.class);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		tt9.hideWindow();
		tt9.startActivity(prefIntent);
	}

	public static void toast(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void toast(Context context, int resourceId) {
		Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show();
	}

	public static void toastLong(Context context, int resourceId) {
		Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
	}

	public static void toastLong(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
