package io.github.sspanak.tt9.chinese;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** The one-line separator, bounded to each candidate's row rather than the whole panel. */
final class CandidateSeparators extends RecyclerView.ItemDecoration {
	private final Drawable separator;
	private final Rect bounds = new Rect();

	CandidateSeparators(Drawable separator) {
		this.separator = separator;
	}

	@Override
	public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		outRect.right = separator.getIntrinsicWidth();
	}

	@Override
	public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		canvas.save();
		canvas.clipRect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getWidth() - parent.getPaddingRight(), parent.getHeight() - parent.getPaddingBottom());
		for (int i = 0; i < parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);
			parent.getDecoratedBoundsWithMargins(child, bounds);
			separator.setBounds(bounds.right - separator.getIntrinsicWidth(), bounds.top, bounds.right, bounds.bottom);
			separator.draw(canvas);
		}
		canvas.restore();
	}
}
