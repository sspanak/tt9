package io.github.sspanak.tt9.chinese;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.db.words.WordStore;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.SupremeExecutor;

/** Chinese word/readings editing; shared import/export remains in Languages. */
public class ChineseUserWordsScreen extends BaseScreenFragment {
	public static final String NAME = "ChineseUserWords";
	private String search = "";
	private long queryVersion;

	public ChineseUserWordsScreen() { super(); }
	public ChineseUserWordsScreen(@Nullable PreferencesActivity activity) { super(activity); }
	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.chinese_user_words_title; }
	@Override protected int getXml() { return R.xml.chinese_user_words; }

	@Override protected void onCreate() {
		if (activity == null) return;
		Preference add = findPreference("chinese_user_add");
		if (add != null) add.setOnPreferenceClickListener(preference -> { ChineseUserWordDialog.show(activity, null, this::refresh); return true; });
		EditTextPreference filter = findPreference("chinese_user_search");
		if (filter != null) filter.setOnPreferenceChangeListener((preference, value) -> {
			search = value.toString().trim(); filter.setSummary(search); refresh(); return true;
		});
		resetFontSize(false);
	}

	@Override public void onResume() { super.onResume(); refresh(); }
	@Override public void onDestroyView() { queryVersion++; super.onDestroyView(); }

	private void refresh() {
		if (activity == null || getPreferenceScreen() == null) return;
		Preference format = findPreference("chinese_user_format");
		if (format != null) format.setSummary(R.string.chinese_user_format);
		long version = ++queryVersion;
		String filter = search;
		PreferencesActivity screen = activity;
		SupremeExecutor.execute(() -> {
			var words = new WordStore(screen.getApplicationContext()).getChineseCustomWords(filter, 200);
			SupremeExecutor.executeOnMainThread(() -> {
				if (version != queryVersion || !isAdded()) return;
				PreferenceCategory list = findPreference("chinese_user_list");
				if (list == null) return;
				list.removeAll();
				for (CustomWord word : words) {
					Preference item = new Preference(screen);
					item.setTitle(word.getDisplayWord());
					item.setSummary(word.getReading().replace('\'', ' '));
					item.setOnPreferenceClickListener(preference -> { ChineseUserWordDialog.show(screen, word, this::refresh); return true; });
					list.addPreference(item);
				}
				if (words.isEmpty()) {
					Preference empty = new Preference(screen);
					empty.setSummary(R.string.chinese_user_empty);
					empty.setEnabled(false);
					list.addPreference(empty);
				}
				resetFontSize(true);
			});
		});
	}
}
