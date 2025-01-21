package io.github.sspanak.tt9.ui.tray;

import android.view.View;
import android.widget.TextView;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.util.Logger;

public class StatusBar {
	private final TextView statusView;
	private String statusText;


	public StatusBar(View mainView) {
		statusView = mainView.findViewById(R.id.status_bar);
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

		statusView.setText(statusText);
	}
}
