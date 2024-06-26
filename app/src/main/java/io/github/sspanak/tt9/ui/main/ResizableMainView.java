package io.github.sspanak.tt9.ui.main;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ResizableMainView extends MainView implements View.OnAttachStateChangeListener {
	private int height;
	private float resizeStartY;
	private long lastResizeTime;

	private int heightNumpad;
	private int heightSmall;
	private int heightTray;


	public ResizableMainView(TraditionalT9 tt9) {
		super(tt9);
		resetHeight();
	}


	private void calculateSnapHeights() {
		heightNumpad = new MainLayoutNumpad(tt9).getHeight();
		heightSmall = new MainLayoutSmall(tt9).getHeight();
		heightTray = new MainLayoutTray(tt9).getHeight();
	}


	private void calculateInitialHeight() {
		if (main == null) {
			return;
		}

		if (tt9.getSettings().isMainLayoutNumpad()) {
			height = heightNumpad;
		} else if (tt9.getSettings().isMainLayoutSmall()) {
			height = heightSmall;
		} else {
			height = heightTray;
		}
	}


	@Override
	public boolean createInputView() {

		if (!super.createInputView()) {
			// recalculate the total height in case the user has changed the key height in the settings
			resetHeight();
			return false;
		}

		main.getView().removeOnAttachStateChangeListener(this);
		main.getView().addOnAttachStateChangeListener(this);

		return true;
	}


	private void onCreateAdjustHeight() {
		if (tt9.getSettings().isMainLayoutNumpad() && height > heightSmall && height <= heightNumpad) {
			setHeight(height, heightSmall, heightNumpad);
		}
	}


	public void onResizeStart(float startY) {
		resizeStartY = startY;
	}


	public void onResize(float currentY) {
		int resizeDelta = (int) (resizeStartY - currentY);
		resizeStartY = currentY;

		if (resizeDelta < 0) {
			shrink(resizeDelta);
		} else if (resizeDelta > 0) {
			expand(resizeDelta);
		}
	}


	public void onSnap() {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutTray()) {
			expand(1);
		} else if (settings.isMainLayoutSmall()) {
			expand(heightNumpad);
		} else {
			shrink(heightNumpad);
		}
	}


	public void onResizeThrottled(float currentY) {
		long now = System.currentTimeMillis();
		if (now - lastResizeTime > SettingsStore.RESIZE_THROTTLING_TIME) {
			lastResizeTime = now;
			onResize(currentY);
		}
	}


	private void expand(int delta) {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutTray()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_SMALL);
			height = heightSmall;
			main = null;
			tt9.initUi();
			vibrate();
		} else if (settings.isMainLayoutSmall()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_NUMPAD);
			height = (int) Math.max(Math.max(heightNumpad * 0.6, heightSmall * 1.1), height + delta);
			main = null;
			tt9.initUi();
			vibrate();
		} else {
			changeHeight(delta, heightSmall, heightNumpad);
		}
	}


	private void shrink(int delta) {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutTray()) {
			return;
		}

		if (settings.isMainLayoutSmall()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_TRAY);
			height = heightTray;
			main = null;
			tt9.initUi();
			vibrate();
		} else if (!changeHeight(delta, heightSmall, heightNumpad)) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_SMALL);
			height = heightSmall;
			main = null;
			tt9.initUi();
			vibrate();
		}
	}


	private boolean changeHeight(int delta, int minHeight, int maxHeight) {
		if (main == null || main.getView() == null) {
			return false;
		}

		return setHeight(main.getView().getMeasuredHeight() + delta, minHeight, maxHeight);
	}


	private boolean setHeight(int height, int minHeight, int maxHeight) {
		if (main == null || main.getView() == null || height < minHeight || height > maxHeight) {
			return false;
		}

		ViewGroup.LayoutParams params = main.getView().getLayoutParams();
		if (params == null) {
			return false;
		}

		params.height = height;
		main.getView().setLayoutParams(params);
		this.height = height;

		return true;
	}


	private void resetHeight() {
		if (main != null) {
			main.resetHeight();
		}

		calculateSnapHeights();
		calculateInitialHeight();
		setHeight(height, heightSmall, heightNumpad);
	}


	private void vibrate() {
		if (tt9.getSettings().getHapticFeedback() && main != null && main.getView() != null) {
			main.getView().performHapticFeedback(Vibration.getPressVibration(null));
		}
	}


	@Override public void onViewAttachedToWindow(@NonNull View v) { onCreateAdjustHeight(); }
	@Override public void onViewDetachedFromWindow(@NonNull View v) {}
}
