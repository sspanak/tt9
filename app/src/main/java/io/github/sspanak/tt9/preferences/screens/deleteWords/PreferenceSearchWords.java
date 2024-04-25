package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceSearchWords extends ScreenPreference {
	public static final String NAME = "dictionary_delete_words_search";
	private static final String LOG_TAG = PreferenceSearchWords.class.getSimpleName();

	private ConsumerCompat<ArrayList<String>> onWords;
	private SettingsStore settings;
	@NonNull private String lastSearchTerm = "";


	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceSearchWords(@NonNull Context context) { super(context); }


	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		EditText editText = holder.itemView.findViewById(R.id.input_text_input_field);
		if (editText == null) {
			Logger.e(LOG_TAG, "Cannot attach a text change listener. Unable to find the EditText element.");
		} else {
			editText.addTextChangedListener(TextChangeListener.getInstance(this::onChange));
		}
	}


	@Override protected int getDefaultLayout() { return R.layout.pref_input_text; }
	@Override protected int getLargeLayout() { return R.layout.pref_input_text_large; }


	@NonNull
	public String getLastSearchTerm() {
		return lastSearchTerm;
	}

	private SettingsStore getSettings() {
		if (settings == null) {
			settings = new SettingsStore(getContext());
		}

		return settings;
	}


	private void onChange(String word) {
		lastSearchTerm = word == null || word.trim().isEmpty() ? "" : word.trim();

		if (onWords == null) {
			Logger.w(LOG_TAG, "No handler set for the word change event.");
		} else if (lastSearchTerm.isEmpty()) {
			Logger.d(LOG_TAG, "Not searching for an empty word.");
			onWords.accept(null);
		} else {
			Language currentLanguage = LanguageCollection.getLanguage(getContext(), getSettings().getInputLanguage());
			WordStoreAsync.getCustomWords(onWords, currentLanguage, lastSearchTerm);
		}
	}


	void setOnWordsHandler(ConsumerCompat<ArrayList<String>> onWords) {
		this.onWords = onWords;
	}
}
