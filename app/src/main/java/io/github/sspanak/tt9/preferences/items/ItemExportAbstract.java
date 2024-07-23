package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

abstract public class ItemExportAbstract extends ItemProcessCustomWordsAbstract {
	public ItemExportAbstract(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
	}

	@Override
	protected String getFailureTitle() {
		return activity.getString(R.string.dictionary_export_failed);
	}

	@Override
	protected String getFailureMessage() {
		return activity.getString(R.string.dictionary_export_failed_more_info);
	}

	@Override
	protected String getSuccessTitle() {
		return activity.getString(R.string.dictionary_export_finished);
	}

	@Override
	protected String getSuccessMessage(String fileName) {
		return activity.getString(R.string.dictionary_export_finished_more_info, fileName);
	}
}
