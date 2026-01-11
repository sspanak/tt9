package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextChangeWatcher;

public class EditWordDialogLetterEditor extends androidx.appcompat.widget.AppCompatEditText {
	@Nullable private TextChangeWatcher changeWatcher;
	@Nullable private Language language;
	@NonNull private String lastText;
	@Nullable private SettingsStore settings;

	@Nullable private Runnable onArrowLeft;
	@Nullable private Runnable onArrowRight;
	@Nullable private Runnable onBackspace;
	@Nullable private Runnable onOK;


	public EditWordDialogLetterEditor(Context context) { super(context); init(context); }
	public EditWordDialogLetterEditor(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
	public EditWordDialogLetterEditor(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }


	private void init(@NonNull Context context) {
		settings = new SettingsStore(context);
		lastText = "";

		changeWatcher = new TextChangeWatcher(this::onTextChange);
		addTextChangedListener(changeWatcher);
	}


	private void onTextChange(Editable ed) {
		if (ed == null || ed.length() == 0) {
			if (!lastText.isEmpty()) {
				setText(lastText);
			}
			return;
		}

		Text newText = new Text(
			language,
			lastText.isEmpty() ? ed.toString() : ed.toString().replaceFirst(lastText, "")
		);

		if (newText.isEmpty()) {
			return;
		}

		// in case the user managed to type more than one letter, keep only the last one
		final int lastGraphemeLength = newText.lastGraphemeLength();
		if (lastGraphemeLength < newText.length()) {
			newText = new Text(
				language,
				newText.toString().substring(0, newText.length() - lastGraphemeLength)
			);
		}


		// allow only letters and numbers, or else revert to last text
		if (newText.isWord() || newText.isNumeric()) {
			setText(newText.toString());
			lastText = newText.toString();
		} else {
			setText(lastText);
		}
	}


	private boolean onArrowLeft(int keyCode, KeyEvent event) {
		if (Key.isArrowLeft(keyCode)) {
			if (isKeyDown(event) && onArrowLeft != null) {
				onArrowLeft.run();
			}
			return true;
		}

		return false;
	}


	private boolean onArrowRight(int keyCode, KeyEvent event) {
		if (Key.isArrowRight(keyCode)) {
			if (isKeyDown(event) && onArrowRight != null) {
				onArrowRight.run();
			}
			return true;
		}

		return false;
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
		if (event.getScanCode() == 0 && event.getEventTime() == 0 && Key.isBackspace(settings, keyCode)) {
			return false;
		}

		return
			onArrowLeft(keyCode, event)
			|| onArrowRight(keyCode, event)
			|| onBackspace(keyCode, event)
			|| onOK(keyCode, event)
			|| keyCode == KeyEvent.KEYCODE_0
			|| super.onKeyDown(keyCode, event);
	}


	/**
	 * Handle soft key up
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getScanCode() == 0 && event.getEventTime() == 0 && Key.isBackspace(settings, keyCode)) {
			return false;
		}

		return
			Key.isArrowLeft(keyCode)
			|| Key.isArrowRight(keyCode)
			|| Key.isBackspace(settings, keyCode)
			|| Key.isOK(keyCode)
			|| keyCode == KeyEvent.KEYCODE_0
			|| super.onKeyUp(keyCode, event);
	}


	/**
	 * Handle hardware keys
	 */
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		return
			onArrowLeft(keyCode, event)
			|| onArrowRight(keyCode, event)
			|| onBackspace(keyCode, event)
			|| onOK(keyCode, event)
			|| keyCode == KeyEvent.KEYCODE_0
			|| super.onKeyPreIme(keyCode, event);
	}


	private boolean isKeyDown(KeyEvent event) {
		return event.getAction() == KeyEvent.ACTION_DOWN;
	}


	@NonNull
	public EditWordDialogLetterEditor setOnArrowLeftListener(@Nullable Runnable listener) {
		onArrowLeft = listener;
		return this;
	}


	@NonNull
	public EditWordDialogLetterEditor setOnArrowRightListener(@Nullable Runnable listener) {
		onArrowRight = listener;
		return this;
	}


	@NonNull
	public EditWordDialogLetterEditor setOnBackspaceListener(@Nullable Runnable listener) {
		onBackspace = listener;
		return this;
	}


	public EditWordDialogLetterEditor setOnOKListener(@Nullable Runnable listener) {
		onOK = listener;
		return this;
	}


	public void setText(@NonNull String text) {
		if (changeWatcher != null) changeWatcher.ignoreNextChange();
		lastText = text;
		super.setText(text);
		setSelection(text.length() - 1 >= 0 ? text.length() : 0);
	}


	public void setLanguage(@Nullable Language language) {
		this.language = language;
	}
}
