package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.ThemedContextBuilder;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract class ThemedPopupDialog extends PopupDialog {
	ThemedPopupDialog(@NonNull Context context, ConsumerCompat<String> activityFinisher, int theme) {
		super(
			new ThemedContextBuilder()
				.setConfiguration(context.getApplicationContext().getResources().getConfiguration())
				.setContext(context)
				.setSettings(new SettingsStore(context))
				// The main theme does not work on Android <= 11 and the _AddWord theme does not work on 12+.
				// Not sure why since they inherit from the same parent, but it is what it is.
				.setTheme(DeviceInfo.AT_LEAST_ANDROID_12 ? R.style.TTheme : theme)
				.build(),
			activityFinisher
		);
	}
}
