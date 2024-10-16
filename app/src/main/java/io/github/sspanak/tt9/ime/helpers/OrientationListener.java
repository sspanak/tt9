package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class OrientationListener extends android.view.OrientationEventListener {
	private final Configuration configuration;
	private final Runnable onChange;

	private int previousOrientation = Configuration.ORIENTATION_UNDEFINED;

	public OrientationListener(@NonNull Context context, @NonNull Runnable onChange) {
		super(context);
		configuration = context.getResources().getConfiguration();
		this.onChange = onChange;
	}

	@Override
	public void onOrientationChanged(int orientation) {
		if (
			(orientation > 15 && orientation < 75)
			|| (orientation > 105 && orientation < 165)
			|| (orientation > 195 && orientation < 255)
			|| (orientation > 285 && orientation < 345)
		) {
			return;
		}

		if (previousOrientation != configuration.orientation) {
			previousOrientation = configuration.orientation;
			onChange.run();
		}
	}

	public void start() {
		if (canDetectOrientation()) {
			enable();
		}
	}

	public void stop() {
		disable();
	}
}
