package io.github.sspanak.tt9.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.LanguageRadioButton;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class ChangeLanguageDialog extends ThemedPopupDialog {
	private final ArrayList<Language> languages;
	private final MainView mainView;
	private final SettingsStore settings;

	private final ConsumerCompat<Integer> onLanguageChanged;
	private Dialog popup;
	private final ArrayList<LanguageRadioButton> radioButtonsCache = new ArrayList<>();


	public ChangeLanguageDialog(@NonNull TraditionalT9 tt9, @Nullable ConsumerCompat<Integer> changeHandler) {
		super(tt9, null, R.style.TTheme_AddWord);

		title = tt9.getResources().getString(R.string.language_popup_title);
		OKLabel = null;

		mainView = tt9.getMainView();
		settings = tt9.getSettings();
		languages = LanguageCollection.getAll(settings.getEnabledLanguageIds(), true);
		onLanguageChanged = changeHandler;
	}


	private void onClick(View button) {
		if (onLanguageChanged != null) {
			onLanguageChanged.accept(button.getId());
		}
		close();
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

		if (onLanguageChanged != null) {
			onLanguageChanged.accept(languageId);
		}
		close();
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
		final View view = View.inflate(context, R.layout.popup_language_select, null);
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


	public void show() {
		popup = new PopupBuilder(context)
			.setCancelable(true)
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(true, this::close)
			.setOnKeyListener(this)
			.setView(generateRadioButtons())
			.showFromIme(mainView);
	}
}
