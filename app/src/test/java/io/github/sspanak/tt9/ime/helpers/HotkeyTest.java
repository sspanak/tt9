package io.github.sspanak.tt9.ime.helpers;

import android.view.KeyEvent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HotkeyTest {
	@Test
	public void disabledSwapLeavesEveryBindingUnchanged() {
		for (int key = -400; key <= 400; key++) {
			assertEquals(key, Hotkey.swapStarAndPound(key, false));
		}
	}


	@Test
	public void swapExchangesShortAndLongPressesAsGroups() {
		assertEquals(KeyEvent.KEYCODE_POUND, Hotkey.swapStarAndPound(KeyEvent.KEYCODE_STAR, true));
		assertEquals(KeyEvent.KEYCODE_STAR, Hotkey.swapStarAndPound(KeyEvent.KEYCODE_POUND, true));
		assertEquals(-KeyEvent.KEYCODE_POUND, Hotkey.swapStarAndPound(-KeyEvent.KEYCODE_STAR, true));
		assertEquals(-KeyEvent.KEYCODE_STAR, Hotkey.swapStarAndPound(-KeyEvent.KEYCODE_POUND, true));
	}


	@Test
	public void unrelatedCustomBindingsAndUnassignedKeysStayUnchanged() {
		int[] keys = {KeyEvent.KEYCODE_UNKNOWN, KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_DEL,
			KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_RIGHT, -KeyEvent.KEYCODE_VOLUME_DOWN, Integer.MIN_VALUE};
		for (int key : keys) {
			assertEquals(key, Hotkey.swapStarAndPound(key, true));
		}
	}


	@Test
	public void displayedBindingCanBeSavedWithoutChangingItsStoredAssignment() {
		for (int stored = -400; stored <= 400; stored++) {
			int displayed = Hotkey.swapStarAndPound(stored, true);
			assertEquals(stored, Hotkey.swapStarAndPound(displayed, true));
		}
	}


	@Test
	public void assigningAPhysicalKeyWhileSwappedUsesTheOriginalOrderForStorage() {
		int storedShortPound = Hotkey.swapStarAndPound(KeyEvent.KEYCODE_POUND, true);
		int storedLongStar = Hotkey.swapStarAndPound(-KeyEvent.KEYCODE_STAR, true);
		assertEquals(KeyEvent.KEYCODE_STAR, storedShortPound);
		assertEquals(-KeyEvent.KEYCODE_POUND, storedLongStar);
		assertEquals(KeyEvent.KEYCODE_POUND, Hotkey.swapStarAndPound(storedShortPound, true));
		assertEquals(-KeyEvent.KEYCODE_STAR, Hotkey.swapStarAndPound(storedLongStar, true));
		assertEquals(KeyEvent.KEYCODE_STAR, Hotkey.swapStarAndPound(storedShortPound, false));
		assertEquals(-KeyEvent.KEYCODE_POUND, Hotkey.swapStarAndPound(storedLongStar, false));
	}
}
