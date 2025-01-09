package io.github.sspanak.tt9.preferences.screens.languageSelection;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.preferences.items.ItemSearch;

public class PreferenceSearchLanguage extends ItemSearch {
	@NonNull private ArrayList<PreferenceSwitchLanguage> languageItems = new ArrayList<>();
	private Preference noResultItem;


	public PreferenceSearchLanguage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public PreferenceSearchLanguage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public PreferenceSearchLanguage(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	public PreferenceSearchLanguage(@NonNull Context context) {
		super(context);
	}


	private void showNoResultItem(boolean show) {
		if (noResultItem != null) {
			noResultItem.setVisible(show);
		}
	}


	@Override
	protected void onTextChange() {
		String word = text.trim().toLowerCase();
		String wordInTheMiddle = " " + word;
		String wordInParenthesis = "(" + word;

		int visibleLanguages = languageItems.size();
		for (PreferenceSwitchLanguage languageItem : languageItems) {
			String title = languageItem.getTitle() == null ? "" : languageItem.getTitle().toString().toLowerCase();
			String summary = languageItem.getSummary() == null ? "" : languageItem.getSummary().toString().toLowerCase();

			languageItem.setVisible(
				// ordered by most likely to be found
				title.startsWith(word) || summary.startsWith(word) ||
				summary.contains(wordInTheMiddle) || title.contains(wordInParenthesis) ||
				summary.contains(wordInParenthesis) || title.contains(wordInTheMiddle)
			);

			if (!languageItem.isVisible()) {
				visibleLanguages--;
			}
		}

		showNoResultItem(visibleLanguages == 0);
	}


	PreferenceSearchLanguage setLanguageItems(@NonNull ArrayList<PreferenceSwitchLanguage> languageItems) {
		this.languageItems = languageItems;
		return this;
	}


	void setNoResultItem(Preference noResultItem) {
		this.noResultItem = noResultItem;
	}
}
