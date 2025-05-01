package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;

public class ChangeLanguageDialog extends ThemedPopupDialog {
	public static final String TYPE = "tt9.popup_dialog.change_language";
	public static final String INTENT_SET_LANGUAGE = "tt9.popup_dialog.command.set_language";

	private final ArrayList<Language> languages;
	private final SettingsStore settings;


	// @todo: layout with radio buttons for touchscreens
	// @todo: maybe styles?
	// @todo: make it cancelable
	// @todo: preferences + translations


	ChangeLanguageDialog(@NonNull Context context, ConsumerCompat<String> activityFinisher) {
		super(context, activityFinisher, R.style.TTheme_AddWord);
		title = context.getResources().getString(R.string.pref_choose_languages);
		OKLabel = null;

		settings = new SettingsStore(context);
		languages = LanguageCollection.getAll(settings.getEnabledLanguageIds(), true);
	}


	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (!Key.isNumber(keyCode)) {
			return false;
		}

		int language = Key.codeToNumber(settings, keyCode) - 1;
		if (language < 0 || language >= languages.size()) {
			return false;
		}

		dialog.dismiss();
		if (activityFinisher != null) {
			activityFinisher.accept(INTENT_SET_LANGUAGE + languages.get(language).getId());
		}

		return true;
	}


	@Override
	void render() {
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < languages.size(); i++) {
			Language lang = languages.get(i);
			tmp.append("\n").append(i+1).append(". ").append(lang.getName());
		}

		message = tmp.toString();
		super.render(() -> {});
	}


	public static void show(InputMethodService ims) {
		Intent intent = new Intent(ims, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_DIALOG_TYPE, TYPE);
		ims.startActivity(intent);
	}
}
