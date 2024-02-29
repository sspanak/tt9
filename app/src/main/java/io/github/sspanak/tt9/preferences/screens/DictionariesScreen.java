package io.github.sspanak.tt9.preferences.screens;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.db.exporter.CustomWordsExporter;
import io.github.sspanak.tt9.db.exporter.DictionaryExporter;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.items.ItemExportCustomWords;
import io.github.sspanak.tt9.preferences.items.ItemExportDictionary;
import io.github.sspanak.tt9.preferences.items.ItemLoadDictionary;
import io.github.sspanak.tt9.preferences.items.ItemSelectLanguage;
import io.github.sspanak.tt9.preferences.items.ItemTruncateAll;
import io.github.sspanak.tt9.preferences.items.ItemTruncateUnselected;

public class DictionariesScreen extends BaseScreenFragment {
	public static final String NAME = "Dictionaries";

	private final ArrayList<ItemClickable> clickables = new ArrayList<>();

	private ItemLoadDictionary loadItem;
	private ItemExportDictionary exportDictionaryItem;
	private ItemExportCustomWords exportCustomWordsItem;

	public DictionariesScreen() { init(); }
	public DictionariesScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
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

		loadItem = new ItemLoadDictionary(findPreference(ItemLoadDictionary.NAME),
			activity,
			activity.settings,
			() -> ItemClickable.disableOthers(clickables, loadItem),
			this::onActionFinish
		);

		exportDictionaryItem = new ItemExportDictionary(findPreference(ItemExportDictionary.NAME),
			activity,
			activity.settings,
			this::onActionStart,
			this::onActionFinish
		);

		clickables.add(loadItem);
		clickables.add(exportDictionaryItem);

		clickables.add(new ItemTruncateUnselected(
			findPreference(ItemTruncateUnselected.NAME),
			activity,
			activity.settings,
			this::onActionStart,
			this::onActionFinish
		));

		clickables.add(new ItemTruncateAll(
			findPreference(ItemTruncateAll.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		));

		exportCustomWordsItem = new ItemExportCustomWords(
			findPreference(ItemExportCustomWords.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish);

		clickables.add(exportCustomWordsItem);

		ItemClickable.enableAllClickHandlers(clickables);
		refreshItems();
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshItems();
	}


	private void refreshItems() {
		loadItem.refreshStatus();
		exportDictionaryItem.refreshStatus();
		exportCustomWordsItem.refreshStatus();

		if (DictionaryLoader.getInstance(activity).isRunning()) {
			loadItem.refreshStatus();
			ItemClickable.disableOthers(clickables, loadItem);
		} else if (CustomWordsExporter.getInstance().isRunning() || DictionaryExporter.getInstance().isRunning()) {
			onActionStart();
		} else {
			onActionFinish();
		}
	}


	private void onActionStart() {
		ItemClickable.disableAll(clickables);
	}

	private void onActionFinish() {
		ItemClickable.enableAll(clickables);
	}
}
