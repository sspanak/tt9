package io.github.sspanak.tt9.ui.main;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

public class ResizableMainView extends MainView implements View.OnAttachStateChangeListener {
	private Vibration vibration;

	private int height;
	private float resizeStartY;
	private long lastResizeTime;

	private int heightNumpad;
	private int heightSmall;
	private int heightTray;


	public ResizableMainView(TraditionalT9 tt9) {
		super(tt9);
		calculateSnapHeights();
	}


	private void calculateSnapHeights() {
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

		vibration = new Vibration(tt9.getSettings(), main.getView());

		return true;
	}


	private void onCreateAdjustHeight() {
		if (tt9.getSettings().isMainLayoutNumpad() && height > heightSmall && height <= heightNumpad) {
			setHeight(height, heightSmall, heightNumpad);
		}
	}

	@Override public void onViewAttachedToWindow(@NonNull View v) { onCreateAdjustHeight(); }
	@Override public void onViewDetachedFromWindow(@NonNull View v) {}


	public void onOrientationChanged() {
		hideCommandPalette();
		calculateSnapHeights();
		render();
	}


	public void onAlign(float deltaX) {
		boolean right = deltaX > 0;
		SettingsStore settings = tt9.getSettings();

		if (settings.getNumpadAlignment() == Gravity.START && right) {
			settings.setNumpadAlignment(Gravity.CENTER_HORIZONTAL);
		} else if (settings.getNumpadAlignment() == Gravity.END && !right) {
			settings.setNumpadAlignment(Gravity.CENTER_HORIZONTAL);
		} else if (settings.getNumpadAlignment() == Gravity.CENTER_HORIZONTAL && right) {
			settings.setNumpadAlignment(Gravity.END);
		} else if (settings.getNumpadAlignment() == Gravity.CENTER_HORIZONTAL && !right) {
			settings.setNumpadAlignment(Gravity.START);
		}

		render();
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


	public void onSnap() {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutTray()) {
			expand(1);
		} else if (settings.isMainLayoutSmall()) {
			expand(heightNumpad);
		} else {
			shrink(-heightNumpad);
		}
	}


	private void expand(int delta) {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutTray()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_SMALL);
			height = heightSmall;
			tt9.onCreateInputView();
			vibration.vibrate();
		} else if (settings.isMainLayoutSmall()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_NUMPAD);
			height = (int) Math.max(Math.max(heightNumpad * 0.6, heightSmall * 1.1), height + delta);
			tt9.onCreateInputView();
			vibration.vibrate();
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
			tt9.onCreateInputView();
			vibration.vibrate();
		} else if (!changeHeight(delta, heightSmall, heightNumpad)) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_SMALL);
			height = heightSmall;
			tt9.onCreateInputView();
			vibration.vibrate();
		}
	}


	private boolean changeHeight(int delta, int minHeight, int maxHeight) {
		if (main == null || main.getView() == null) {
			return false;
		}

		return setHeight(main.getView().getMeasuredHeight() + delta, minHeight, maxHeight);
	}


	private boolean setHeight(int height, int minHeight, int maxHeight) {
		if (main == null || main.getView() == null || height < minHeight) {
			return false;
		}

		height = Math.min(height, maxHeight);

		ViewGroup.LayoutParams params = main.getView().getLayoutParams();
		if (params == null) {
			return false;
		}

		params.height = height;
		main.getView().setLayoutParams(params);
		this.height = height;

		return true;
	}

	private void fitMain() {
		calculateSnapHeights();
		int heightLow, heightHigh, heightMain = main.getHeight(true);

		if (main instanceof MainLayoutNumpad) {
			heightLow = heightSmall;
			heightHigh = heightNumpad;
		} else if (main instanceof MainLayoutSmall) {
			heightLow = 0;
			heightHigh = Math.max(heightSmall, heightMain); // make room for the command palette
		} else {
			heightLow = 0;
			heightHigh = Math.max(heightTray, heightMain); // make room for the command palette
		}

		setHeight(heightMain, heightLow, heightHigh);
	}

	@Override
	public void showCommandPalette() {
		super.showCommandPalette();
		fitMain();
	}

	@Override
	public void hideCommandPalette() {
		super.hideCommandPalette();
		fitMain();
	}

	@Override
	public void showTextEditingPalette() {
		super.showTextEditingPalette();
		fitMain();
	}

	@Override
	public void hideTextEditingPalette() {
		super.hideTextEditingPalette();
		fitMain();
	}

	@Override
	public void render() {
		super.render();
		fitMain();
	}
}
