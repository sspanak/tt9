package io.github.sspanak.tt9.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.util.ThemedContextBuilder;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract public class PopupDialog implements DialogInterface.OnKeyListener {
	protected final ContextThemeWrapper context;
	private final MainView mainView;

	protected Dialog popup;
	protected String title;
	protected String message;
	protected String OKLabel;

	PopupDialog(@NonNull TraditionalT9 tt9, int theme) {
		this.context = new ThemedContextBuilder()
				.setConfiguration(tt9.getResources().getConfiguration())
				.setContext(tt9.getApplicationContext())
				// The main theme does not work on Android <= 11 and the _AddWord theme does not work on 12+.
				// Not sure why since they inherit from the same parent, but it is what it is.
				.setTheme(DeviceInfo.AT_LEAST_ANDROID_12 ? R.style.TTheme : theme)
				.build();

		mainView = tt9.getMainView();
	}

	protected void close() {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		return false;
	}

	protected boolean render(Runnable onOK, Runnable onCancel, View customView) {
		PopupBuilder popupBuilder = new PopupBuilder(context);
		if (onOK != null) {
			popupBuilder.setPositiveButton(OKLabel, onOK);
		}
		if (customView != null) {
			popupBuilder.setView(customView);
		}

		popup = popupBuilder
			.setCancelable(true)
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(true, onCancel)
			.setOnKeyListener(this)
			.showFromIme(mainView);

		return popup != null;
	}
}
