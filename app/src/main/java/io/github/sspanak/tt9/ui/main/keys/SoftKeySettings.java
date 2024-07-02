package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

public class SoftKeySettings extends SoftKey {
	private final ResizeHandle resizeHandle = new ResizeHandle(getContext(), this::showSettings);

	public SoftKeySettings(Context context) {
		super(context);
		setOnLongClickListener(null);
	}

	public SoftKeySettings(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnLongClickListener(null);
	}

	public SoftKeySettings(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOnLongClickListener(null);
	}

	public void setMainView(ResizableMainView mainView) {
		resizeHandle.setMainView(mainView);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		resizeHandle.onTouch(view, event);
		return super.onTouch(view, event);
	}

	protected void showSettings() {
		if (validateTT9Handler()) {
			tt9.showSettings();
		}
	}

	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_settings;
	}
}
