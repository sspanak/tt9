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

	private final int heightNumpad;
	private final int heightSmall;
	private final int heightTray;


	public ResizableMainView(TraditionalT9 tt9) {
		super(tt9);

		heightNumpad = new MainLayoutNumpad(tt9).getHeight();
		heightSmall = new MainLayoutSmall(tt9).getHeight();
		heightTray = new MainLayoutTray(tt9).getHeight();
	}


	@Override
	public boolean createInputView() {
		if (!super.createInputView()) {
			return false;
		}

		main.getView().removeOnAttachStateChangeListener(this);
		main.getView().addOnAttachStateChangeListener(this);

		return true;
	}


	private void onResizeAdjustHeight() {
		if (tt9.getSettings().isMainLayoutNumpad() && height > heightSmall && height < heightNumpad) {
			setHeight((int) Math.max(heightNumpad * 0.6, heightSmall * 1.1), heightSmall, heightNumpad);
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
			height = heightSmall + 1;
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


	private void vibrate() {
		if (tt9.getSettings().getHapticFeedback() && main != null && main.getView() != null) {
			main.getView().performHapticFeedback(Vibration.getPressVibration(null));
		}
	}


	@Override public void onViewAttachedToWindow(@NonNull View v) { onResizeAdjustHeight(); }
	@Override public void onViewDetachedFromWindow(@NonNull View v) {}
}
