package io.github.sspanak.tt9.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.util.ConsumerCompat;

public class ChangeLanguageDialog extends ThemedPopupDialog {
	public static final String TYPE = "tt9.popup_dialog.change_language";
	public static final String INTENT_SET_LANGUAGE = "tt9.popup_dialog.command.set_language";

	private final LayoutInflater inflater;
	private final ArrayList<Language> languages;
	private final SettingsStore settings;

	private Dialog popup;


	// @todo: maybe styles?
	// @todo: preferences
	// @todo: translations


	ChangeLanguageDialog(@NonNull AppCompatActivity context, ConsumerCompat<String> activityFinisher) {
		super(context, activityFinisher, R.style.TTheme_AddWord);
		title = context.getResources().getString(R.string.pref_choose_languages);
		OKLabel = null;

		inflater = context.getLayoutInflater();
		settings = new SettingsStore(context);
		languages = LanguageCollection.getAll(settings.getEnabledLanguageIds(), true);
	}


	private void onSelect(int languageId) {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}

		if (activityFinisher != null) {
			activityFinisher.accept(INTENT_SET_LANGUAGE + languageId);
		}
	}


	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (!Key.isNumber(keyCode)) {
			return false;
		}

		int selection = Key.codeToNumber(settings, keyCode) - 1;
		if (selection < 0 || selection >= languages.size()) {
			return false;
		}

		onSelect(languages.get(selection).getId());
		return true;
	}


	private void onChecked(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			onSelect(buttonView.getId());
		}
	}


	private View generateRadioButtons() {
		View view = inflater.inflate(R.layout.popup_language_selection, null);
		RadioGroup radioGroup = view.findViewById(R.id.radio_group_language_list);

		for (int i = 0; i < languages.size(); i++) {
			final String text = (i + 1) + ". " + languages.get(i).getName();

			RadioButton radioButton = new RadioButton(context);
			radioButton.setId(languages.get(i).getId());
			radioButton.setOnCheckedChangeListener(this::onChecked);
			radioButton.setText(text);
			radioGroup.addView(radioButton);
		}

		return view;
	}


	@Override
	void render() {
		popup = new PopupBuilder(context)
			.setCancelable(true)
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
