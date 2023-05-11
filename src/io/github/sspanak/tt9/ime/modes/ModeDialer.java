package io.github.sspanak.tt9.ime.modes;

import io.github.sspanak.tt9.ime.helpers.Key;

// see: InputType.isDialer()
public class ModeDialer extends Mode123 {
	@Override
	public int getId() {
		return MODE_DIALER;
	}

	@Override
	public boolean isDialer() {
		return true;
	}

	@Override
	public boolean onOtherKey(int key) {
		return !Key.isDecimalSeparator(key) && super.onOtherKey(key);
	}
}
