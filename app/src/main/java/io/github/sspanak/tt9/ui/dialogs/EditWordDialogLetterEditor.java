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

public class EditWordDialogLetterEditor extends androidx.appcompat.widget.AppCompatEditText {
	private SettingsStore settings;
	private TextChangeWatcher changeWatcher;

	@Nullable private Runnable onBackspace;
	@Nullable private Runnable onOK;


	public EditWordDialogLetterEditor(Context context) { super(context); init(context); }
	public EditWordDialogLetterEditor(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
	public EditWordDialogLetterEditor(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }


	private void init(@NonNull Context context) {
		settings = new SettingsStore(context);

		changeWatcher = new TextChangeWatcher(this::onTextChange);
		addTextChangedListener(changeWatcher);
	}


	private void onTextChange(Editable text) {
		if (text != null && text.length() > 1) {
			String letter = text.toString().substring(text.toString().length() - 2, text.toString().length() - 1);
			setText(letter);
		}
	}


	private boolean onOK(int keyCode, KeyEvent event) {
		if (Key.isOK(keyCode)) {
			if (isKeyDown(event) && onOK != null) {
				onOK.run();
			}
			return true;
		}

		return false;
	}


	private boolean onBackspace(int keyCode, KeyEvent event) {
		if (Key.isBackspace(settings, keyCode)) {
			if (isKeyDown(event) && onBackspace != null) {
				onBackspace.run();
			}
			return true;
		}

		return false;
	}


	/**
	 * Handle soft key down
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getScanCode() == 0 && event.getEventTime() == 0) {
			return false;
		}

		return
			onOK(keyCode, event)
			|| onBackspace(keyCode, event)
			|| super.onKeyDown(keyCode, event);
	}


	/**
	 * Handle soft key up
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getScanCode() == 0 && event.getEventTime() == 0) {
			return false;
		}

		return
			Key.isOK(keyCode)
			|| Key.isBackspace(settings, keyCode)
			|| super.onKeyUp(keyCode, event);
	}


	/**
	 * Handle hardware keys
	 */
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		return
			onOK(keyCode, event)
			|| onBackspace(keyCode, event)
			|| super.onKeyPreIme(keyCode, event);
	}


	private boolean isKeyDown(KeyEvent event) {
		return event.getAction() == KeyEvent.ACTION_DOWN;
	}


	@NonNull
	public EditWordDialogLetterEditor setOnBackspaceListener(@Nullable Runnable listener) {
		onBackspace = listener;
		return this;
	}


	public void setOnOKListener(@Nullable Runnable listener) {
		onOK = listener;
	}


	public void setText(@NonNull String text) {
		changeWatcher.ignoreNextChange();
		super.setText(text);
	}
}
