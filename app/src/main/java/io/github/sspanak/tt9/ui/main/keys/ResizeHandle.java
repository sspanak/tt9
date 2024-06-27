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

	private final float RESIZE_THRESHOLD;
	private boolean dragging;
	private float startY;


	ResizeHandle(@NonNull Context context, @NonNull Runnable onClick) {
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
		startY = event.getRawY();
	}

	private void handleDrag(MotionEvent event) {
		if (mainView == null) {
			dragging = false;
			return;
		}

		if (!dragging && Math.abs(event.getRawY() - startY) >= RESIZE_THRESHOLD) {
			mainView.onResizeStart(event.getRawY());
			dragging = true;
		} else if (dragging) {
			mainView.onResizeThrottled(event.getRawY());
		}
	}

	private void handleRelease(MotionEvent event) {
		if (mainView != null && dragging) {
			mainView.onResize(event.getRawY());
			dragging = false;
		} else {
			onClick.run();
		}
	}
}
