package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.items.ItemTextInput;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract class AbstractPreferenceCharList extends ItemTextInput {
	@NonNull protected String currentChars = "";
	protected Language language;
	private Runnable onRender;
	protected static SettingsStore settings;


	AbstractPreferenceCharList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	AbstractPreferenceCharList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	AbstractPreferenceCharList(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	AbstractPreferenceCharList(@NonNull Context context) {
		super(context);
	}

	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		if (holder.itemView.findViewById(R.id.input_text_input_field) != null && onRender != null) {
			onRender.run();
		}
	}

	public void setOnRender(Runnable onRender) {
		this.onRender = onRender;
	}

	protected SettingsStore getSettings() {
		if (settings == null) {
			settings = new SettingsStore(getContext());
		}
		return settings;
	}

	@Override
	protected void onChange(String word) {
		currentChars = word == null ? "" : word;
		validateCurrentChars();
	}

	void onLanguageChanged(Language language) {
		this.language = language;

		String all = getChars();
		char[] mandatory = getMandatoryChars();
		StringBuilder optional = new StringBuilder();

		for (int i = 0; i < all.length(); i++) {
			char c = all.charAt(i);

			boolean isMandatory = false;
			for (char m : mandatory) {
				if (m == c) {
					isMandatory = true;
					break;
				}
			}

			if (!isMandatory) {
				optional.append(c);
			}
		}

		setText(optional.toString());
	}

	@NonNull abstract protected String getChars();
	@NonNull abstract protected char[] getMandatoryChars();
	abstract protected boolean validateCurrentChars();
	abstract protected void saveCurrentChars();
}
