package io.github.sspanak.tt9.ui.tray;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.R;

public class StatusBar {
	private final TextView statusView;
	private String statusText;


	public StatusBar(View mainView) {
		statusView = mainView.findViewById(R.id.status_bar);
	}


	public void setText(String text) {
		statusText = "[ " + text + " ]";
		this.render();
	}


	public void setDarkTheme(boolean darkTheme) {
		if (statusView == null) {
			Logger.w("StatusBar.setDarkTheme", "Not changing the theme of a NULL View.");
			return;
		}

		Context context = statusView.getContext();

		int backgroundColor = ContextCompat.getColor(
			context,
			darkTheme ? R.color.dark_candidate_background : R.color.candidate_background
		);
		int color = ContextCompat.getColor(
			context,
			darkTheme ? R.color.dark_candidate_color : R.color.candidate_color
		);

		statusView.setBackgroundColor(backgroundColor);
		statusView.setTextColor(color);
		this.render();
	}


	private void render() {
		if (statusText == null) {
			Logger.w("StatusBar.render", "Not displaying NULL status");
			return;
		}

		statusView.setText(statusText);
	}
}
