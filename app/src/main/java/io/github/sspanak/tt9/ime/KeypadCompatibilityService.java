package io.github.sspanak.tt9.ime;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import io.github.sspanak.tt9.preferences.screens.debug.DropDownInputHandlingMode;
import io.github.sspanak.tt9.util.sys.SystemSettings;

/** Opt-in filter for firmware that handles STAR before the active input method. */
public final class KeypadCompatibilityService extends AccessibilityService {
	private static WeakReference<TraditionalT9> input = new WeakReference<>(null);
	private static WeakReference<KeypadCompatibilityService> service = new WeakReference<>(null);
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable hold = this::onHold;
	private WeakReference<TraditionalT9> owner = new WeakReference<>(null);
	private KeyEvent down;
	private boolean claimed;
	private int claimedDevice;
	private long claimedDownTime;


	static void inputStarted(TraditionalT9 tt9) {
		KeypadCompatibilityService filter = service.get();
		if (filter != null) filter.cancelKey();
		input = new WeakReference<>(tt9);
	}


	static void inputFinished(TraditionalT9 tt9) {
		if (input.get() != tt9) return;
		input.clear();
		KeypadCompatibilityService filter = service.get();
		if (filter != null) filter.cancelKey();
	}


	/** The bound-service list may temporarily be empty during startup or UI automation. */
	@Nullable
	public static Boolean isEnabled(Context context) {
		ComponentName component = new ComponentName(context, KeypadCompatibilityService.class);
		try {
			String configured = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (configured == null) return false;
			for (String id : configured.split(":")) {
				if (component.equals(ComponentName.unflattenFromString(id))) return true;
			}
			return false;
		} catch (SecurityException exception) {
			return null; // An unreadable setting is not evidence that the user disabled the service.
		}
	}


	@Override
	public void onCreate() {
		super.onCreate();
		service = new WeakReference<>(this);
	}


	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		if (event.getKeyCode() != KeyEvent.KEYCODE_STAR) return false;
		if (claimed && (event.getDeviceId() != claimedDevice || event.getDownTime() != claimedDownTime)) {
			if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() != 0
				|| event.getDeviceId() != claimedDevice) return false;
			// Recover if a device lost the previous UP; never consume another keyboard's release.
			cancelKey();
			claimed = false;
		}
		if (event.getAction() == KeyEvent.ACTION_UP) {
			if (!claimed) return false;
			TraditionalT9 tt9 = owner.get();
			if (!event.isCanceled() && canHandle(tt9)) {
				tt9.onKeyUp(KeyEvent.KEYCODE_STAR, event);
				finishKey();
			} else cancelKey();
			claimed = false;
			return true;
		}
		if (event.getAction() != KeyEvent.ACTION_DOWN) return claimed;
		if (claimed) {
			if (!canHandle(owner.get())) cancelKey();
			return true;
		}
		// Never claim the middle of a key stream or events from an on-screen keyboard.
		if (event.getRepeatCount() != 0 || event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD
			|| (event.getFlags() & KeyEvent.FLAG_SOFT_KEYBOARD) != 0) return false;
		TraditionalT9 tt9 = input.get();
		if (!canHandle(tt9)) return false;
		claimed = true;
		claimedDevice = event.getDeviceId();
		claimedDownTime = event.getDownTime();
		owner = new WeakReference<>(tt9);
		down = new KeyEvent(event);
		tt9.onKeyDown(KeyEvent.KEYCODE_STAR, down);
		if (down != null && tt9.isHoldHotkey(-KeyEvent.KEYCODE_STAR)) {
			handler.postDelayed(hold, ViewConfiguration.getLongPressTimeout());
		}
		return true;
	}


	private boolean canHandle(TraditionalT9 tt9) {
		EditorInfo editor = tt9 == null ? null : tt9.getCurrentInputEditorInfo();
		return tt9 != null && tt9 == input.get() && editor != null && editor.inputType != EditorInfo.TYPE_NULL
			&& !tt9.shouldBeOff() && tt9.getSettings().getStarKeyCompatibility()
			&& tt9.getSettings().getInputHandlingMode() == DropDownInputHandlingMode.NORMAL
			&& SystemSettings.isTT9Selected(tt9);
	}


	private void onHold() {
		TraditionalT9 tt9 = owner.get();
		if (down == null || !canHandle(tt9)) {
			cancelKey();
			return;
		}
		KeyEvent event = KeyEvent.changeTimeRepeat(down, SystemClock.uptimeMillis(), 1,
			down.getFlags() | KeyEvent.FLAG_LONG_PRESS);
		tt9.onKeyLongPress(KeyEvent.KEYCODE_STAR, event);
	}


	private void cancelKey() {
		TraditionalT9 tt9 = owner.get();
		if (tt9 != null) tt9.cancelInterceptedKey(KeyEvent.KEYCODE_STAR);
		finishKey();
		// Keep consuming an already claimed stream through its UP, even after focus changes.
	}


	private void finishKey() {
		handler.removeCallbacks(hold);
		owner.clear();
		down = null;
	}


	@Override public void onAccessibilityEvent(AccessibilityEvent event) {}
	@Override public void onInterrupt() { cancelKey(); }

	@Override
	public void onDestroy() {
		cancelKey();
		if (service.get() == this) service.clear();
		super.onDestroy();
	}
}
