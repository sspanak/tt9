package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.colors.AccentSystemColor;
import io.github.sspanak.tt9.util.colors.ErrorSystemColor;

abstract public class TextInputPreference extends ScreenPreference {
	@NonNull private final Handler listener = new Handler(Looper.getMainLooper());
	protected EditText textField;
	@NonNull protected String text = "";


	public TextInputPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public TextInputPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public TextInputPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	public TextInputPreference(@NonNull Context context) {
		super(context);
	}


	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		setTextField(holder);
		if (textField != null) {
			ignoreEnter();
			checkTextChange();
		}
	}


	@Override protected int getDefaultLayout() { return R.layout.pref_input_text; }
	@Override protected int getLargeLayout() { return R.layout.pref_input_text_large; }


	protected int getChangeHandlerDebounceTime() {
		return SettingsStore.TEXT_INPUT_DEBOUNCE_TIME;
	}


	protected void setError(String error) {
		if (textField == null) {
			return;
		}

		final boolean noError = error == null || error.isEmpty();
		textField.setError(noError ? null : error);

		int color = noError ? new AccentSystemColor(getContext()).get() : new ErrorSystemColor(getContext()).get();
		textField.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
	}


	public CharSequence getText() {
		return text;
	}


	protected void setText(CharSequence newText) {
		if (textField != null && newText != null && !text.equals(newText.toString())) {
			textField.setText(newText);
			text = newText.toString();
		}
	}


	protected void setTextField(@NonNull PreferenceViewHolder holder) {
		EditText editText = holder.itemView.findViewById(R.id.input_text_input_field);
		if (editText != null) {
			this.textField = editText;
		}
	}


	/**
	 * Internal text change detector that calls the onTextChange() when needed.
	 * IMPORTANT: do not call this method more than once per instance to avoid creating multiple
	 * listeners and memory leaks.
	 */
	private void checkTextChange() {
		String newText = textField != null ? textField.getText().toString() : "";
		if (!text.equals(newText)) {
			text = newText;
			onTextChange();
		}
		listener.postDelayed(this::checkTextChange, getChangeHandlerDebounceTime());
	}


	/**
	 * This prevents IllegalStateException "focus search returned a view that wasn't able to take focus!",
	 * which is thrown when the EditText is focused and it receives a simulated ENTER key event.
	 */
	private void ignoreEnter() {
		textField.setOnKeyListener((v, keyCode, e) -> keyCode == KeyEvent.KEYCODE_ENTER);
	}


	abstract protected void onTextChange();
}
