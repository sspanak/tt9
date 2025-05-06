package io.github.sspanak.tt9.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.LanguageRadioButton;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class ChangeLanguageDialog extends ThemedPopupDialog {
	public static final String TYPE = "tt9.popup_dialog.change_language";
	public static final String INTENT_SET_LANGUAGE = "tt9.popup_dialog.command.set_language";

	private final LayoutInflater inflater;
	private final ArrayList<Language> languages;
	private final SettingsStore settings;

	private Dialog popup;
	private final ArrayList<LanguageRadioButton> radioButtons = new ArrayList<>();


	ChangeLanguageDialog(@NonNull AppCompatActivity context, ConsumerCompat<String> activityFinisher) {
		super(context, activityFinisher, R.style.TTheme_AddWord);
		title = context.getResources().getString(R.string.language_popup_title);
		OKLabel = null;

		inflater = context.getLayoutInflater();
		settings = new SettingsStore(context);
		languages = LanguageCollection.getAll(settings.getEnabledLanguageIds(), true);
	}


	private void onClick(View button) {
		changeLanguage(button.getId());
	}


	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (Key.isBack(keyCode)) {
			close();
			return true;
		}

		int languageId = -1;

		if (Key.isOK(keyCode)) {
			languageId = getSelected();
		} else if (Key.isNumber(keyCode)) {
			languageId = getByIndex(Key.codeToNumber(settings, keyCode) - 1);
		} else if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
			for (LanguageRadioButton radio : radioButtons) radio.autoHighlightCompat(); // yet another device hack
		}

		if (languageId == -1) {
			return false;
		}

		changeLanguage(languageId);
		return true;
	}


	private int getSelected() {
		for (LanguageRadioButton radio : radioButtons) {
			if (radio.hasFocus()) {
				return radio.getId();
			}
		}

		return -1;
	}


	private int getByIndex(int index) {
		return (index < 0 || index >= languages.size()) ? -1 : languages.get(index).getId();
	}


	private void changeLanguage(int languageId) {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}

		if (activityFinisher != null) {
			activityFinisher.accept(INTENT_SET_LANGUAGE + languageId);
		}
	}


	private View generateRadioButtons() {
		final int currentLanguageId = settings.getInputLanguage();
		final View view = inflater.inflate(R.layout.popup_language_select, null);
		final LinearLayout radioGroup = view.findViewById(R.id.language_select_list);

		radioButtons.clear();

		for (int i = 0; i < languages.size(); i++) {
			final String labelPrefix = DeviceInfo.noKeyboard(context) ? null : (i + 1) + ". ";

			LanguageRadioButton radioButton = new LanguageRadioButton(context)
				.setLanguage(languages.get(i), labelPrefix)
				.setChecked(languages.get(i).getId() == currentLanguageId)
				.setOnClick(this::onClick);
			radioGroup.addView(radioButton);
			radioButtons.add(radioButton);
		}

		return view;
	}


	@Override
	void render() {
		popup = new PopupBuilder(context)
			.setCancelable(false)
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(true, this::close)
			.setOnKeyListener(this)
			.setView(generateRadioButtons())
			.show();
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
