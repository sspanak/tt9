package io.github.sspanak.tt9.ui.tray;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;
import io.github.sspanak.tt9.ui.notifications.DictionaryLoadingBar;
import io.github.sspanak.tt9.util.Logger;

public class StatusBar {
	private boolean isShown = true;
	private double lastClickTime = 0;

	@NonNull private final ResizableMainView mainView;
	@Nullable private final TextView statusView;
	@NonNull private final SettingsStore settings;
	@Nullable private String statusText;

	@NonNull private final DictionaryLoadingBar loadingBar;
	@NonNull private final Runnable onLoadingFinished;


	public StatusBar(@NonNull Context context, @NonNull SettingsStore settings, @NonNull ResizableMainView mainView, @NonNull Runnable onDictionaryLoadingFinished) {
		this.mainView = mainView;
		this.settings = settings;
		statusView = mainView.getView() != null ? mainView.getView().findViewById(R.id.status_bar) : null;
		if (statusView != null) {
			statusView.setOnTouchListener(this::onTouch);
		}

		loadingBar = DictionaryLoadingBar.getInstance(context);
		loadingBar.setOnStatusChange2(this::onLoading);
		onLoadingFinished = onDictionaryLoadingFinished;
	}


	/**
	 * Handle double-click and drag resizing
	 */
	private boolean onTouch(View v, MotionEvent event) {
		if (!isShown) {
			return false;
		}

		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mainView.onResizeStart(event.getRawY());
				return true;
			case MotionEvent.ACTION_MOVE:
				if (settings.getDragResize()) {
					mainView.onResizeThrottled(event.getRawY());
				}
				return true;
			case MotionEvent.ACTION_UP:
				long now = System.currentTimeMillis();
				if (settings.getDoubleTapResize() && now - lastClickTime < SettingsStore.SOFT_KEY_DOUBLE_CLICK_DELAY) {
					mainView.onSnap();
				} else if (settings.getDragResize()) {
					mainView.onResize(event.getRawY());
				}

				lastClickTime = now;

				return true;
		}

		return false;
	}


	public boolean isErrorShown() {
		return statusText != null && statusText.startsWith("❌");
	}


	public StatusBar setColorScheme() {
		if (statusView != null) {
			statusView.setTextColor(settings.getKeyboardTextColor());
		}
		return this;
	}


	public void setError(String error) {
		setText("❌  " + error);
	}


	public void setText(int stringResourceId) {
		if (statusView != null) {
			setText(statusView.getContext().getString(stringResourceId));
		}
	}


	public void setText(String text) {
		statusText = text;
		this.render();
	}


	public void setText(InputMode inputMode) {
		setText("[ " + inputMode.toString() + " ]");
	}


	public void setText(VoiceInputOps voiceInputOps) {
		setText("[ " + voiceInputOps.toString() + " ]");
	}


	public void setShown(boolean yes) {
		if (isShown != yes) {
			isShown = yes;
			render();
		}
	}


	private void onLoading() {
		setText("[ " + loadingBar.getShortMessage() + " ]");
		if (loadingBar.isCancelled() || loadingBar.isFailed() || !loadingBar.inProgress()) {
			onLoadingFinished.run();
		}
	}


	private void render() {
		if (statusView == null) {
			return;
		}

		if (statusText == null) {
			Logger.w("StatusBar.render", "Not displaying NULL status");
			return;
		}

		if (!isShown) {
			statusView.setText(null);
			return;
		}

		SpannableString scaledText = new SpannableString(statusText);
		scaledText.setSpan(new RelativeSizeSpan(settings.getSuggestionFontScale()), 0, statusText.length(), 0);

		statusView.setText(scaledText);
	}
}
