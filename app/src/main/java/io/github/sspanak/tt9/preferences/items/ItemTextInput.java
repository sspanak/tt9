package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

abstract public class ItemTextInput extends ScreenPreference implements TextWatcher {
	@NonNull private final Handler debouncer = new Handler(Looper.getMainLooper());

	public ItemTextInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public ItemTextInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ItemTextInput(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ItemTextInput(@NonNull Context context) {
		super(context);
	}

	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		EditText editText = holder.itemView.findViewById(R.id.input_text_input_field);
		if (editText == null) {
			Logger.e(getClass().getSimpleName(), "Cannot attach a text change listener. Unable to find the EditText element.");
		} else {
			editText.addTextChangedListener(this);
			editText.setOnKeyListener(this::ignoreEnter);
		}
	}

	@Override protected int getDefaultLayout() { return R.layout.pref_input_text; }
	@Override protected int getLargeLayout() { return R.layout.pref_input_text_large; }

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		debouncer.removeCallbacksAndMessages(null);
		debouncer.postDelayed(() -> onChange(s.toString()), SettingsStore.TEXT_INPUT_DEBOUNCE_TIME);
	}

	/**
	 * This prevents IllegalStateException "focus search returned a view that wasn't able to take focus!",
	 * which is thrown when the EditText is focused and it receives a simulated ENTER key event.
	 */
	private boolean ignoreEnter(View v, int keyCode, KeyEvent e) {
		return keyCode == KeyEvent.KEYCODE_ENTER;
	}

	protected abstract void onChange(String word);
}
