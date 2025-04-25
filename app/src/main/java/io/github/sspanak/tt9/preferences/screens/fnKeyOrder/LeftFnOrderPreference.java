package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.preferences.items.TextInputPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class LeftFnOrderPreference extends TextInputPreference {
	public final static String NAME = "pref_lfn_key_order";

	protected final SettingsStore settings;
	@NonNull private Runnable textChangeHandler = () -> {};

	public LeftFnOrderPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.settings = new SettingsStore(context);
	}
	public LeftFnOrderPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.settings = new SettingsStore(context);
	}
	public LeftFnOrderPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		this.settings = new SettingsStore(context);
	}
	public LeftFnOrderPreference(@NonNull Context context) {
		super(context);
		this.settings = new SettingsStore(context);
	}

	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		populate();
		if (textField != null) {
			textField.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
	}

	void setTextChangeHandler(@NonNull Runnable handler) {
		textChangeHandler = handler;
	}

	protected int getChangeHandlerDebounceTime() {
		return SettingsStore.TEXT_INPUT_PUNCTUATION_ORDER_DEBOUNCE_TIME;
	}

	@Override
	protected void onTextChange() {
		textChangeHandler.run();
	}

	protected void populate() {
		setText(settings.getLfnKeyOrder());
	}

	@Override
	public void setError(@NonNull String error) {
		super.setError(error);
	}

	void setText(@NonNull String newText) {
		super.setText(newText);
	}
}
