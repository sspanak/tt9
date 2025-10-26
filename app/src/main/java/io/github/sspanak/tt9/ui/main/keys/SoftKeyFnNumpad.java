package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFnNumpad extends BaseSwipeableKey {
	protected static final float TITLE_SCALE_BOPOMOFO = 0.7f;

	private Command holdCommand = null;

	private final static SparseArray<Integer> NUMBERS = new SparseArray<>() {{
		put(R.id.soft_key_0, 0); // short space
		put(R.id.soft_key_200, 0); // long space
		put(R.id.soft_key_1, 1);
		put(R.id.soft_key_2, 2);
		put(R.id.soft_key_3, 3);
		put(R.id.soft_key_4, 4);
		put(R.id.soft_key_5, 5);
		put(R.id.soft_key_6, 6);
		put(R.id.soft_key_7, 7);
		put(R.id.soft_key_8, 8);
		put(R.id.soft_key_9, 9);
	}};

	private final static SparseArray<Integer> UPSIDE_DOWN_NUMBERS = new SparseArray<>() {{
		put(1, 7);
		put(2, 8);
		put(3, 9);
		put(7, 1);
		put(8, 2);
		put(9, 3);
	}};


	public SoftKeyFnNumpad(Context context) { super(context); }
	public SoftKeyFnNumpad(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFnNumpad(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected void initColors(@NonNull SettingsStore settings) {
		backgroundColor = settings.getKeyBackgroundColor();
		cornerElementColor = settings.getKeyCornerElementColor();
		rippleColor = settings.getKeyRippleColor();
		centralIconColor = textColor = settings.getKeyTextColor();
	}

	@Override
	protected void handleHold() {
		preventRepeat();

		int keyCode = Key.numberToCode(getUpsideDownNumber(getId()));
		if (keyCode < 0 || !validateTT9Handler()) {
			return;
		}

		vibrate(Vibration.getHoldVibration());
		tt9.onKeyLongPress(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		tt9.onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
	}


	@Override
	protected boolean handleRelease() {
		int keyCode = Key.numberToCode(getUpsideDownNumber(getId()));
		if (keyCode < 0 || !validateTT9Handler()) {
			return false;
		}

		tt9.onKeyDown(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		tt9.onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));

		return true;
	}


	protected int getNumber() {
		return getNumber(getId());
	}


	protected int getNumber(int keyId) {
		return NUMBERS.get(keyId, -1);
	}


	protected int getUpsideDownNumber(int keyId) {
		int number = getNumber(keyId);

		if (tt9 == null || !tt9.getSettings().getUpsideDownKeys()) {
			return number;
		}

		return UPSIDE_DOWN_NUMBERS.get(number, number);
	}


	protected String getLocalizedNumber(int number) {
		if (tt9 != null && !tt9.isInputModeNumeric() && tt9.getLanguage() != null) {
			return tt9.getLanguage().getKeyNumeral(number);
		} else {
			return String.valueOf(number);
		}
	}


	@Nullable
	protected Command getHoldCommand() {
		if (tt9 == null) {
			return null;
		}

		String currentCommandId = tt9.getSettings().getFunction(-Key.numberToCode(getNumber()));
		if (holdCommand == null || !holdCommand.getId().equals(currentCommandId)) {
			holdCommand = CommandCollection.getById(CommandCollection.COLLECTION_HOTKEYS, currentCommandId);
			resetIconCache();
		}

		return holdCommand;
	}


	@Override
	protected float getCornerElementScale(int position) {
		if (position == ICON_POSITION_TOP_RIGHT && tt9 != null && LanguageKind.isArabic(tt9.getLanguage())) {
			return super.getCornerElementScale(ICON_POSITION_TOP_RIGHT) * 1.25f;
		}

		return super.getCornerElementScale(position);
	}
}
