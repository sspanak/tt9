package io.github.sspanak.tt9.preferences.screens.languages;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

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

	public LanguagesScreen() { super(); }
	public LanguagesScreen(@Nullable PreferencesActivity activity) { super(activity); }

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
		addClickable(loadItem);
		addClickable(exportDictionaryItem);
		addClickable(truncateUnselectedItem);
		addClickable(truncateAllItem);

		addClickable(new ItemDeleteCustomWords(findPreference(ItemDeleteCustomWords.NAME)));

		exportCustomWordsItem = new ItemExportCustomWords(
			findPreference(ItemExportCustomWords.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);
		addClickable(exportCustomWordsItem);

		importCustomWordsItem = new ItemImportCustomWords(
			findPreference(ItemImportCustomWords.NAME),
			activity,
			this::onActionStart,
			this::onActionFinish
		);
		addClickable(importCustomWordsItem);

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


	protected void addClickable(ItemClickable item) {
		clickables.add(item);
	}


	protected boolean isProcessorRunning() {
		return
			CustomWordsExporter.getInstance().isRunning()
			|| DictionaryExporter.getInstance().isRunning()
			|| CustomWordsImporter.getInstance(activity).isRunning()
			|| DictionaryDeleter.getInstance(activity).isRunning();
	}


	protected void refreshItems() {
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
		} else if (isProcessorRunning()) {
			onActionStart();
		} else {
			onActionFinish();
		}
	}


	protected void onActionStart() {
		ItemClickable.disableAll(clickables);
	}

	protected void onActionFinish() {
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
