package io.github.sspanak.tt9.preferences;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.ui.UI;


public class ItemTruncateDictionary extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	private final Context context;
	private final DictionaryLoader loader;
	private final ItemLoadDictionary loadItem;


	ItemTruncateDictionary(Preference item, ItemLoadDictionary loadItem, Context context, DictionaryLoader loader) {
		super(item);
		this.context = context;
		this.loadItem = loadItem;
		this.loader = loader;
	}

	private final Handler onDictionaryTruncated = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			UI.toast(context, R.string.dictionary_truncated);
		}
	};

	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			loader.stop();
			loadItem.changeToLoadButton();
		}

		DictionaryDb.truncateWords(onDictionaryTruncated);

		return false;
	}
}
