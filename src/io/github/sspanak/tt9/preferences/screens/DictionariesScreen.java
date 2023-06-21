package io.github.sspanak.tt9.preferences.screens;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemLoadDictionary;
import io.github.sspanak.tt9.preferences.items.ItemSelectLanguage;
import io.github.sspanak.tt9.preferences.items.ItemTruncateAll;
import io.github.sspanak.tt9.preferences.items.ItemTruncateUnselected;

public class DictionariesScreen extends BaseScreenFragment {
	public DictionariesScreen() { init(); }
	public DictionariesScreen(PreferencesActivity activity) { init(activity); }

	@Override protected int getTitle() { return R.string.pref_choose_languages; }
	@Override protected int getXml() { return R.xml.prefs_screen_dictionaries; }

	@Override
	protected void onCreate() {
		ItemSelectLanguage multiSelect = new ItemSelectLanguage(
			activity,
			findPreference(ItemSelectLanguage.NAME),
			activity.settings
		);
		multiSelect.populate().enableValidation();

		ItemLoadDictionary loadItem = new ItemLoadDictionary(
			findPreference(ItemLoadDictionary.NAME),
			activity,
			activity.settings,
			activity.getDictionaryLoader(),
			activity.getDictionaryProgressBar()
		);
		loadItem.enableClickHandler();

		ItemTruncateAll truncateItem = new ItemTruncateAll(
			findPreference(ItemTruncateAll.NAME),
			loadItem,
			activity,
			activity.getDictionaryLoader()
		);

		ItemTruncateUnselected truncateSelectedItem = new ItemTruncateUnselected(
			findPreference(ItemTruncateUnselected.NAME),
			loadItem,
			activity,
			activity.settings,
			activity.getDictionaryLoader()
		);

		truncateItem.setOtherTruncateItem(truncateSelectedItem).enableClickHandler();
		truncateSelectedItem.setOtherTruncateItem(truncateItem).enableClickHandler();
	}
}
