package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

public class OrientationListener extends android.view.OrientationEventListener {
	private static final short ORIENTATION_LANDSCAPE = 1;
	private static final short ORIENTATION_UNKNOWN = 0;
	private static final short ORIENTATION_PORTRAIT = -1;

	private short previousOrientation = ORIENTATION_UNKNOWN;
	private final Runnable onChange;

	public OrientationListener(@NonNull Context context, @NonNull Runnable onChange) {
		super(context);
		this.onChange = onChange;
	}

	@Override
	public void onOrientationChanged(int orientation) {
		short currentOrientation;

		if (orientation > 345 || orientation < 15 || (orientation > 165 && orientation < 195)) {
			currentOrientation = ORIENTATION_PORTRAIT;
		} else if ((orientation > 75 && orientation < 105) || (orientation > 255 && orientation < 285)) {
			currentOrientation = ORIENTATION_LANDSCAPE;
		} else {
			return;
		}

		if (currentOrientation != previousOrientation) {
			previousOrientation = currentOrientation;
			onChange.run();
		}
	}

	public void start() {
		if (canDetectOrientation()) {
			enable();
		}
	}
}
