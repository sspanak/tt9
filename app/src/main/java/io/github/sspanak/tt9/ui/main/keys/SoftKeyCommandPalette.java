package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

public class SoftKeyCommandPalette extends SoftKey {
	private final ResizeHandle resizeHandle = new ResizeHandle(getContext(), this::showCommandPalette);

	public SoftKeyCommandPalette(Context context) { super(context); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	public void setMainView(ResizableMainView mainView) {
		resizeHandle.setMainView(mainView);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		resizeHandle.onTouch(view, event);
		return super.onTouch(view, event);
	}

	protected void showCommandPalette() {
		if (validateTT9Handler()) {
			tt9.onKeyCommandPalette(false);
		}
	}

	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_command_palette;
	}
}
