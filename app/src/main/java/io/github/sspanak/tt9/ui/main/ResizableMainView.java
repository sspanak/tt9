package io.github.sspanak.tt9.ui.main;

import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

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
		boolean forceRecalculate = DeviceInfo.AT_LEAST_ANDROID_15;

		heightNumpad = new MainLayoutNumpad(tt9).getHeight(forceRecalculate);
		heightSmall = new MainLayoutSmall(tt9).getHeight(forceRecalculate);
		heightTray = new MainLayoutTray(tt9).getHeight(forceRecalculate);
	}


	@Override
	public boolean create() {
		if (!super.create()) {
			return false;
		}

		if (main == null) {
			return false;
		}

		main.getView().removeOnAttachStateChangeListener(this);
		main.getView().addOnAttachStateChangeListener(this);

		vibration = new Vibration(tt9.getSettings(), main.getView());

		return true;
	}


	@Override
	public void destroy() {
		if (main != null && main.getView() != null) {
			main.getView().removeOnAttachStateChangeListener(this);
		}
		super.destroy();
	}


	@Override public void onViewDetachedFromWindow(@NonNull View v) {}
	@Override public void onViewAttachedToWindow(@NonNull View v) {
		if (main != null) {
			main.preventEdgeToEdge();
			setHeight(height, heightSmall, heightNumpad);
		}
	}


	public void onOrientationChanged() {
		showKeyboard();
		render();
	}


	public void onAlign(float deltaX) {
		if (!(main instanceof MainLayoutNumpad)) {
			return;
		}

		boolean right = deltaX > 0;
		SettingsStore settings = tt9.getSettings();

		if (settings.getAlignment() == Gravity.START && right) {
			settings.setAlignment(Gravity.CENTER_HORIZONTAL);
		} else if (settings.getAlignment() == Gravity.END && !right) {
			settings.setAlignment(Gravity.CENTER_HORIZONTAL);
		} else if (settings.getAlignment() == Gravity.CENTER_HORIZONTAL && right) {
			settings.setAlignment(Gravity.END);
		} else if (settings.getAlignment() == Gravity.CENTER_HORIZONTAL && !right) {
			settings.setAlignment(Gravity.START);
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
		if (main == null) {
			return;
		}

		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutTray()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_SMALL);
			height = heightSmall;
			tt9.onCreateInputView();
			main.requestPreventEdgeToEdge();
			vibration.vibrate();
		} else if (settings.isMainLayoutSmall()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_NUMPAD);
			height = (int) Math.max(Math.max(heightNumpad * 0.6, heightSmall * 1.1), height + delta);
			tt9.onCreateInputView();
			main.requestPreventEdgeToEdge();
			vibration.vibrate();
		} else {
			changeHeight(delta, heightSmall, heightNumpad);
		}
	}


	private void shrink(int delta) {
		SettingsStore settings = tt9.getSettings();

		if (main == null || settings.isMainLayoutTray()) {
			return;
		}

		if (settings.isMainLayoutSmall()) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_TRAY);
			height = heightTray;
			tt9.onCreateInputView();
			main.requestPreventEdgeToEdge();
			vibration.vibrate();
		} else if (!changeHeight(delta, heightSmall, heightNumpad)) {
			settings.setMainViewLayout(SettingsStore.LAYOUT_SMALL);
			height = heightSmall;
			tt9.onCreateInputView();
			main.requestPreventEdgeToEdge();
			vibration.vibrate();
		}
	}


	private boolean changeHeight(int delta, int minHeight, int maxHeight) {
		int keyboardHeight = main != null ? main.getKeyboardHeight() : -1;
		if (keyboardHeight == 0) {
			return false;
		}

		return setHeight(keyboardHeight + delta, minHeight, maxHeight);
	}


	private boolean setHeight(int height, int minHeight, int maxHeight) {
		if (main == null || height < minHeight) {
			return false;
		}

		height = Math.min(height, maxHeight);
		if (main.setKeyboardHeight(height)) {
			this.height = height;
			return true;
		}

		return false;
	}

	private void fitMain() {
		if (main == null || main instanceof MainLayoutStealth) {
			return;
		}

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
	public void showKeyboard() {
		super.showKeyboard();
		fitMain();
	}

	@Override
	public void showTextEditingPalette() {
		super.showTextEditingPalette();
		fitMain();
	}

	@Override
	public void render() {
		super.render();
		fitMain();
	}
}
