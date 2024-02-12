package io.github.sspanak.tt9.preferences.screens;

import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemLoadDictionary;
import io.github.sspanak.tt9.preferences.items.ItemSelectLanguage;
import io.github.sspanak.tt9.preferences.items.ItemTruncateAll;
import io.github.sspanak.tt9.preferences.items.ItemTruncateUnselected;

public class DictionariesScreen extends BaseScreenFragment {
	private ItemLoadDictionary loadItem;

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

		loadItem = new ItemLoadDictionary(
			findPreference(ItemLoadDictionary.NAME),
			activity,
			activity.settings,
			activity.getDictionaryLoader(),
			activity.getDictionaryProgressBar()
		);

		ItemTruncateUnselected deleteItem = new ItemTruncateUnselected(
			findPreference(ItemTruncateUnselected.NAME),
			activity,
			activity.settings,
			activity.getDictionaryLoader()
		);

		ItemTruncateAll truncateItem = new ItemTruncateAll(
			findPreference(ItemTruncateAll.NAME),
			activity,
			activity.getDictionaryLoader()
		);

		loadItem.setOtherItems(Arrays.asList(truncateItem, deleteItem)).enableClickHandler();
		deleteItem.setOtherItems(Arrays.asList(truncateItem, loadItem)).enableClickHandler();
		truncateItem.setOtherItems(Arrays.asList(deleteItem, loadItem)).enableClickHandler();
	}

	@Override
	public void onResume() {
		super.onResume();
		loadItem.refreshStatus();
	}
}
