package io.github.sspanak.tt9.preferences.screens.languages;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.CustomWordsExporter;
import io.github.sspanak.tt9.db.customWords.CustomWordsImporter;
import io.github.sspanak.tt9.db.customWords.DictionaryExporter;
import io.github.sspanak.tt9.db.words.DictionaryDeleter;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class LanguagesScreen extends BaseScreenFragment {
	public static final String NAME = "Languages";

	private static final ArrayList<ItemClickable> clickables = new ArrayList<>();

	private ItemLoadDictionary loadItem;
	private ItemImportCustomWords importCustomWordsItem;
	private ItemExportDictionary exportDictionaryItem;
	private ItemExportCustomWords exportCustomWordsItem;
	private ItemTruncateAll truncateAllItem;
	private ItemTruncateUnselected truncateUnselectedItem;

	public LanguagesScreen() { init(); }
	public LanguagesScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_choose_languages; }
	@Override protected int getXml() { return R.xml.prefs_screen_languages; }


	@Override
	protected void onCreate() {
		new ItemSelectLanguage(activity, findPreference(ItemSelectLanguage.NAME)).populate();

		loadItem = new ItemLoadDictionary(
			findPreference(ItemLoadDictionary.NAME),
			activity,
			() -> ItemClickable.disableOthers(clickables, loadItem),
			this::onActionFinish
		);

		exportDictionaryItem = new ItemExportDictionary(
			findPreference(ItemExportDictionary.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);

		truncateUnselectedItem = new ItemTruncateUnselected(
			findPreference(ItemTruncateUnselected.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);

		truncateAllItem = new ItemTruncateAll(
			findPreference(ItemTruncateAll.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);

		clickables.clear();
		clickables.add(loadItem);
		clickables.add(exportDictionaryItem);
		clickables.add(truncateUnselectedItem);
		clickables.add(truncateAllItem);

		clickables.add(new ItemDeleteCustomWords(findPreference(ItemDeleteCustomWords.NAME)));

		exportCustomWordsItem = new ItemExportCustomWords(
			findPreference(ItemExportCustomWords.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);
		clickables.add(exportCustomWordsItem);

		importCustomWordsItem = new ItemImportCustomWords(
			findPreference(ItemImportCustomWords.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);
		clickables.add(importCustomWordsItem);

		ItemClickable.enableAllClickHandlers(clickables);
		refreshItems();

		resetFontSize(false);
		createBrowseFilesLauncher();
	}


	@Override
	public void onResume() {
		super.onResume();
		refreshItems();
	}


	private void refreshItems() {
		new ItemSelectLanguage(activity, findPreference(ItemSelectLanguage.NAME)).populate();
		loadItem.refreshStatus();
		exportDictionaryItem.refreshStatus();
		exportCustomWordsItem.refreshStatus();
		importCustomWordsItem.refreshStatus();
		truncateUnselectedItem.refreshStatus();
		truncateAllItem.refreshStatus();

		if (DictionaryLoader.getInstance(activity).isRunning()) {
			loadItem.refreshStatus();
			ItemClickable.disableOthers(clickables, loadItem);
		} else if (
			CustomWordsExporter.getInstance().isRunning()
			|| DictionaryExporter.getInstance().isRunning()
			|| CustomWordsImporter.getInstance(activity).isRunning()
			|| DictionaryDeleter.getInstance(activity).isRunning()
		) {
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

	private void createBrowseFilesLauncher() {
		ActivityResultLauncher<Intent> launcher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> importCustomWordsItem.onFileSelected(result)
		);

		importCustomWordsItem.setBrowseFilesLauncher(launcher);
	}
}
