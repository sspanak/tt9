package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

public class ResizeHandle implements View.OnTouchListener {
	@NonNull private final Runnable onClick;
	private ResizableMainView mainView;

	private final float ALIGN_THRESHOLD;
	private final float RESIZE_THRESHOLD;

	private boolean aligning;
	private boolean resizing;
	private float startX;
	private float startY;


	ResizeHandle(@NonNull Context context, @NonNull Runnable onClick) {
		ALIGN_THRESHOLD = context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height) * 0.75f;
		RESIZE_THRESHOLD = context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height) / 4.0f;
		this.onClick = onClick;
	}

	public void setMainView(ResizableMainView mainView) {
		this.mainView = mainView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				handlePress(event);
				break;
			case MotionEvent.ACTION_MOVE:
				handleDrag(event);
				break;
			case MotionEvent.ACTION_UP:
				handleRelease(event);
				break;
		}

		return false;
	}

	private void handlePress(MotionEvent event) {
		startX = event.getRawX();
		startY = event.getRawY();
	}

	private void handleDrag(MotionEvent event) {
		if (mainView == null) {
			aligning = false;
			resizing = false;
			return;
		}

		if (!resizing && Math.abs(event.getRawY() - startY) >= RESIZE_THRESHOLD) {
			mainView.onResizeStart(event.getRawY());
			resizing = true;
		} else if (resizing) {
			mainView.onResizeThrottled(event.getRawY());
		} else if (!aligning && Math.abs(event.getRawX() - startX) >= ALIGN_THRESHOLD) {
			mainView.onAlign(event.getRawX() - startX);
			aligning = true;
		}
	}

	private void handleRelease(MotionEvent event) {
		if (mainView != null && resizing) {
			mainView.onResize(event.getRawY());
			resizing = false;
		} else if (mainView != null && aligning) {
			aligning = false;
		} else {
			onClick.run();
		}
	}
}
