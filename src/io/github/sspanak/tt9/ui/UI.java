package io.github.sspanak.tt9.ui;

import android.content.Intent;
import android.util.Log;

import io.github.sspanak.tt9.LangHelper;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.KeyPadHandler;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class UI {
	private static AbsSymDialog mSmileyPopup = null;
	private static AbsSymDialog mSymbolPopup = null;


	public static void showAddWordDialog(KeyPadHandler tt9, int language, String currentWord) {
		Intent awIntent = new Intent(tt9, AddWordAct.class);
		awIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		awIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		awIntent.putExtra("io.github.sspanak.tt9.word", currentWord);
		awIntent.putExtra("io.github.sspanak.tt9.lang", language);
		tt9.startActivity(awIntent);
	}


	public static void showSymbolDialog(KeyPadHandler tt9) {
		if (mSymbolPopup == null) {
			mSymbolPopup = new SymbolDialog(tt9, tt9.getLayoutInflater().inflate(R.layout.symbolview,
					null));
		}
		mSymbolPopup.doShow(tt9.getWindow().getWindow().getDecorView());
	}


	public static void showSmileyDialog(KeyPadHandler tt9) {
		if (mSmileyPopup == null) {
			mSmileyPopup = new SmileyDialog(tt9, tt9.getLayoutInflater().inflate(R.layout.symbolview,
					null));
		}
		mSmileyPopup.doShow(tt9.getWindow().getWindow().getDecorView());
	}


	public static void showPreferencesScreen(KeyPadHandler tt9) {
		Intent prefIntent = new Intent(tt9, TraditionalT9Settings.class);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		tt9.hideWindow();
		tt9.startActivity(prefIntent);
	}


	/**
	 * updateStatusIcon
	 * Set the status icon that is appropriate in current mode (based on
	 * openwmm-legacy)
	 */
	public static void updateStatusIcon(KeyPadHandler tt9, int inputMode, int capsMode) {
		switch (inputMode) {
			case T9Preferences.MODE_ABC:
				// @todo: show the proper status icon
				tt9.showStatusIcon(LangHelper.ICONMAP[0][inputMode][capsMode]);
				break;
			case T9Preferences.MODE_PREDICTIVE:
				// @todo: show the proper status icon
				tt9.showStatusIcon(LangHelper.ICONMAP[0][inputMode][capsMode]);
				break;
			case T9Preferences.MODE_123:
				tt9.showStatusIcon(R.drawable.ime_number);
				break;
			default:
				Log.i("updateStatusIcon", "Unknown inputMode mode: " + inputMode + ". Hiding status icon.");
				tt9.hideStatusIcon();
				break;
		}
	}
}
