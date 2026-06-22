package io.github.sspanak.tt9.util.sys;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import java.util.function.Consumer;

import io.github.sspanak.tt9.util.SupremeExecutor;


/**
 * Detects fold state of the device and notifies the caller about changes. On non-foldable devices,
 * it will report "folded" state once, when created, and then do nothing.
 */
public class FoldDetector {
	@NonNull private final Consumer<Boolean> externalCallback;
	@Nullable private final WindowInfoTrackerCallbackAdapter windowInfoTracker;


	public FoldDetector(@NonNull Context context, @NonNull Consumer<Boolean> onFoldStateChanged) {
		externalCallback = onFoldStateChanged;

		if (!HardwareInfo.isFoldable(context)) {
			windowInfoTracker = null;
			externalCallback.accept(true);
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
		for (DisplayFeature feature : layoutInfo.getDisplayFeatures()) {
			if (feature instanceof FoldingFeature) {
				externalCallback.accept(isFolded((FoldingFeature) feature));
				return;
			}
		}
		externalCallback.accept(true);
	}


	private boolean isFolded(@NonNull FoldingFeature foldFeature) {
		return !(foldFeature.getState() == FoldingFeature.State.HALF_OPENED || foldFeature.getState() == FoldingFeature.State.FLAT);
	}
}
