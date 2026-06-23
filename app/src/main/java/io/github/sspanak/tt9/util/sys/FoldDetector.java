package io.github.sspanak.tt9.util.sys;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import io.github.sspanak.tt9.util.SupremeExecutor;


/**
 * Detects fold state of the device and notifies the caller about changes. On non-foldable devices,
 * it will never notify about changes and will always return true for isFolded() method.
 */
public class FoldDetector {
	@NonNull private final Runnable externalCallback;
	@Nullable private final WindowInfoTrackerCallbackAdapter windowInfoTracker;
	private boolean isFolded = true;


	public FoldDetector(@NonNull Context context, @NonNull Runnable onFoldStateChanged) {
		externalCallback = onFoldStateChanged;

		if (!HardwareInfo.isFoldable(context)) {
			windowInfoTracker = null;
			return;
		}

		windowInfoTracker = new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(context));
		windowInfoTracker.addWindowLayoutInfoListener(
			context,
			SupremeExecutor::executeOnMainThread,
			this::onLayoutInfoChanged
		);
	}


	public void destroy() {
		if (windowInfoTracker != null) {
			windowInfoTracker.removeWindowLayoutInfoListener(this::onLayoutInfoChanged);
		}
	}


	private void onLayoutInfoChanged(@NonNull WindowLayoutInfo layoutInfo) {
		isFolded = true;

		for (DisplayFeature feature : layoutInfo.getDisplayFeatures()) {
			if (feature instanceof FoldingFeature) {
				isFolded = isFolded((FoldingFeature) feature);
				break;
			}
		}

		externalCallback.run();
	}


	private boolean isFolded(@NonNull FoldingFeature foldFeature) {
		return !(foldFeature.getState() == FoldingFeature.State.HALF_OPENED || foldFeature.getState() == FoldingFeature.State.FLAT);
	}


	public boolean isFolded() {
		return isFolded;
	}
}
