package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextChangeWatcher;

public class EditWordLetterEditor extends androidx.appcompat.widget.AppCompatEditText {
	private SettingsStore settings;
	private TextChangeWatcher changeWatcher;

	@Nullable private Runnable onBackspace;
	@Nullable private Runnable onOK;

	public EditWordLetterEditor(Context context) { super(context); init(context); }
	public EditWordLetterEditor(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
	public EditWordLetterEditor(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }

	private void init(@NonNull Context context) {
		settings = new SettingsStore(context);

		changeWatcher = new TextChangeWatcher(this::onTextChange);
		addTextChangedListener(changeWatcher);
	}


	private void onTextChange(Editable text) {
		if (text != null && text.length() > 1) {
			String letter = text.toString().substring(text.toString().length() - 2, text.toString().length() - 1);
			setTextSilent(letter);
		}
	}


	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (Key.isOK(keyCode)) {
			if (isKeyDown(event) && onOK != null) {
				onOK.run();
			}
			return true;
		}

		if (Key.isBackspace(settings, keyCode)) {
			if (isKeyDown(event) && onBackspace != null) {
				onBackspace.run();
			}
			return true;
		}

		return super.onKeyPreIme(keyCode, event);
	}


	private boolean isKeyDown(KeyEvent event) {
		return event.getAction() == KeyEvent.ACTION_DOWN;
	}


	@NonNull
	public EditWordLetterEditor setOnBackspaceListener(@Nullable Runnable listener) {
		onBackspace = listener;
		return this;
	}


	public void setOnOKListener(@Nullable Runnable listener) {
		onOK = listener;
	}


	public void setTextSilent(@NonNull String text) {
		changeWatcher.ignoreNextChange();
		setText(text);
	}
}

