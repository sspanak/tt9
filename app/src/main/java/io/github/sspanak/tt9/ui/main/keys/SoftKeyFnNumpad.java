package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.commands.CommandCollection;

public class SoftKeyFnNumpad extends SoftKeyNumberNumpad {
	public SoftKeyFnNumpad(Context context) { super(context); }
	public SoftKeyFnNumpad(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFnNumpad(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	protected boolean isFnPanelOn() {
		return isCommandPaletteActive();
	}

	protected boolean isVoiceInput() {
		return CommandCollection.indexOf(CommandCollection.COLLECTION_PALETTE, CmdVoiceInput.ID) == getId();
	}

	protected boolean isTextEditing() {
		return CommandCollection.indexOf(CommandCollection.COLLECTION_PALETTE, CmdEditText.ID) == getId();
	}

	protected boolean isNOOP() {
		return !CommandCollection.getAll(CommandCollection.COLLECTION_PALETTE).containsKey(getId());
	}

	protected boolean isCommandAvailable() {
		if (isNOOP()) {
			return false;
		} else if (isVoiceInput()) {
			return tt9 == null || !tt9.isVoiceInputMissing();
		} else if (isTextEditing()) {
			return tt9 == null || !tt9.isInputLimited();
		} else {
			return true;
		}
	}

	protected boolean isCommandPaletteActive() {
		return tt9 != null && tt9.isCommandPaletteActive();
	}

	protected void handleHold() {
		if (isCommandPaletteActive()) {
			preventRepeat();
		} else {
			super.handleHold();
		}
	}

	@Override
	protected String getTitle() {
		return isCommandPaletteActive() ? "" : super.getTitle();
	}

	@Override
	protected String getHoldText() {
		return isCommandPaletteActive() ? "" : super.getHoldText();
	}

	@Override
	protected int getCentralIcon() {
		if (!isCommandPaletteActive()) {
			return super.getCentralIcon();
		}

		return CommandCollection.getByKeyId(CommandCollection.COLLECTION_PALETTE, getId()).getIcon();
	}

	@Override
	public void render() {
		if (isFnPanelOn()) {
			resetIconCache();
		}

		setVisibility(!isCommandPaletteActive() || isCommandAvailable() ? VISIBLE : INVISIBLE);

		super.render();
	}
}
