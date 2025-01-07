package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.db.customWords.AbstractFileProcessor;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.notifications.DictionaryProgressNotification;

abstract public class ItemProcessCustomWordsAbstract extends ItemClickable {
	final protected PreferencesActivity activity;
	final private Runnable onStart;
	final private Runnable onFinish;


	public ItemProcessCustomWordsAbstract(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item);
		this.activity = activity;
		this.onStart = onStart;
		this.onFinish = onFinish;

		AbstractFileProcessor processor = getProcessor();
		processor.setFailureHandler(() -> onFinishProcessing(null));
		processor.setStartHandler(() -> activity.runOnUiThread(this::setBusy));
		processor.setSuccessHandler(this::onFinishProcessing);
	}


	abstract protected AbstractFileProcessor getProcessor();


	public ItemProcessCustomWordsAbstract refreshStatus() {
		if (item != null) {
			if (getProcessor().isRunning()) {
				setBusy();
			} else {
				enable();
			}
		}
		return this;
	}


	@Override
	protected boolean onClick(Preference p) {
		setBusy();
		if (!onStartProcessing()) {
			setAndNotifyReady();
		}
		return true;
	}


	abstract protected boolean onStartProcessing();


	protected void onFinishProcessing(String fileName) {
		activity.runOnUiThread(() -> {
			setAndNotifyReady();

			if (fileName == null) {
				DictionaryProgressNotification.getInstance(activity).showError(
					getFailureTitle(),
					getFailureMessage()
				);
			} else {
				DictionaryProgressNotification.getInstance(activity).showMessage(
					getSuccessTitle(),
					getSuccessMessage(fileName),
					getSuccessMessage(fileName)
				);
			}
		});
	}


	abstract protected String getFailureMessage();
	abstract protected String getFailureTitle();
	abstract protected String getSuccessMessage(String fileName);
	abstract protected String getSuccessTitle();


	protected void setBusy() {
		if (onStart != null) onStart.run();
		disable();

		String loadingMessage = getProcessor().getStatusMessage();
		item.setSummary(loadingMessage);
		DictionaryProgressNotification.getInstance(activity).showLoadingMessage(loadingMessage, "");
	}

	protected void setAndNotifyReady() {
		enable();
		if (onFinish != null) onFinish.run();
	}
}
