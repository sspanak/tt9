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
import java.util.HashMap;

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
	public static final String PARAMETER_LANGUAGE = "tt9.popup_dialog.parameter.language";
	public static final String PARAMETER_SEQUENCE = "tt9.popup_dialog.parameter.sequence";
	public static final String PARAMETER_WORD = "tt9.popup_dialog.parameter.word";

	private final ConsumerCompat<HashMap<String, String>> activityFinisher;
	private final LayoutInflater inflater;
	private final ArrayList<Language> languages;
	private final SettingsStore settings;
	private final String currentSequence;
	private final String currentWord;

	private final ArrayList<LanguageRadioButton> radioButtonsCache = new ArrayList<>();
	private Dialog popup;


	ChangeLanguageDialog(@NonNull AppCompatActivity context, @NonNull Intent intent, ConsumerCompat<HashMap<String, String>> activityFinisher) {
		super(context, null, R.style.TTheme_AddWord);

		this.activityFinisher = activityFinisher;
		title = context.getResources().getString(R.string.language_popup_title);
		OKLabel = null;

		inflater = context.getLayoutInflater();
		settings = new SettingsStore(context);
		languages = LanguageCollection.getAll(settings.getEnabledLanguageIds(), true);

		currentSequence = intent.getStringExtra(PARAMETER_SEQUENCE);
		currentWord = intent.getStringExtra(PARAMETER_WORD);
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
			for (LanguageRadioButton radio : radioButtonsCache) radio.autoHighlightCompat(); // yet another device hack
		}

		if (languageId == -1) {
			return false;
		}

		changeLanguage(languageId);
		return true;
	}


	private int getSelected() {
		for (LanguageRadioButton radio : radioButtonsCache) {
			if (radio.hasFocus()) {
				return radio.getId();
			}
		}

		return -1;
	}


	private int getByIndex(int index) {
		return (index < 0 || index >= languages.size()) ? -1 : languages.get(index).getId();
	}


	@Override
	protected void close() {
		detachRadioButtons();
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
		super.close();
	}


	private void changeLanguage(int languageId) {
		detachRadioButtons();
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}

		if (activityFinisher != null) {
			HashMap<String, String> messages = new HashMap<>();
			messages.put(INTENT_SET_LANGUAGE, INTENT_SET_LANGUAGE);
			messages.put(PARAMETER_LANGUAGE, String.valueOf(languageId));
			messages.put(PARAMETER_SEQUENCE, currentSequence);
			messages.put(PARAMETER_WORD, currentWord);
			activityFinisher.accept(messages);
		}
	}


	private void detachRadioButtons() {
		for (LanguageRadioButton radio : radioButtonsCache) {
			radio.setOnClick(null);
			LinearLayout parent = (LinearLayout) radio.getParent();
			if (parent != null) {
				parent.removeView(radio);
			}
		}
	}


	private View generateRadioButtons() {
		final int currentLanguageId = settings.getInputLanguage();
		final View view = inflater.inflate(R.layout.popup_language_select, null);
		final LinearLayout radioGroup = view.findViewById(R.id.language_select_list);

		radioButtonsCache.clear();

		for (int i = 0, end = languages.size(); i < end; i++) {
			final String labelPrefix = DeviceInfo.noKeyboard(context) ? null : (i + 1) + ". ";

			LanguageRadioButton radioButton = new LanguageRadioButton(context)
				.setOnClick(this::onClick)
				.setLanguage(languages.get(i), labelPrefix)
				.setChecked(languages.get(i).getId() == currentLanguageId);

			radioButtonsCache.add(radioButton);
			radioGroup.addView(radioButton);
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


	/**
	 * Open a popup dialog containing a list of the enabled languages. After a language is selected,
	 * "currentSequence" and "currentWord" are passed back to the IME, in case it wants to recompose them.
	 */
	public static void show(InputMethodService ims, String currentSequence, String currentWord) {
		Intent intent = new Intent(ims, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_DIALOG_TYPE, TYPE);
		intent.putExtra(PARAMETER_SEQUENCE, currentSequence);
		intent.putExtra(PARAMETER_WORD, currentWord);
		ims.startActivity(intent);
	}
}
