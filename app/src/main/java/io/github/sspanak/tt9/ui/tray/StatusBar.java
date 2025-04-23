package io.github.sspanak.tt9.ui.tray;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class StatusBar {
	@Nullable private final TextView statusView;
	@NonNull private final SettingsStore settings;
	@Nullable private String statusText;


	public StatusBar(@NonNull SettingsStore settings, @Nullable View mainView) {
		this.settings = settings;
		statusView = mainView != null ? mainView.findViewById(R.id.status_bar) : null;
	}


	public boolean isErrorShown() {
		return statusText != null && statusText.startsWith("❌");
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


	private void render() {
		if (statusView == null) {
			return;
		}

		if (statusText == null) {
			Logger.w("StatusBar.render", "Not displaying NULL status");
			return;
		}

		SpannableString scaledText = new SpannableString(statusText);
		scaledText.setSpan(new RelativeSizeSpan(settings.getSuggestionFontScale()), 0, statusText.length(), 0);

		statusView.setText(scaledText);
	}
}
