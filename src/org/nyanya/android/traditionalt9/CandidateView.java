package org.nyanya.android.traditionalt9;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CandidateView extends View {

	private List<String> mSuggestions;
	protected int mSelectedIndex;

	private Drawable mSelectionHighlight;

	private Rect mBgPadding;

	private static final int MAX_SUGGESTIONS = 32;
	private static final int SCROLL_PIXELS = 20;

	private int[] mWordWidth = new int[MAX_SUGGESTIONS];
	private int[] mWordX = new int[MAX_SUGGESTIONS];

	private static final int X_GAP = 10;

	private static final List<String> EMPTY_LIST = new ArrayList<String>();

	private int mColorNormal;
	private int mColorRecommended;
	private int mColorOther;
	private int mVerticalPadding;
	private Paint mPaint;
	private int mTargetScrollX;

	private int mTotalWidth;

	Rect mPadding;

	/**
	 * Construct a CandidateView for showing suggested words for completion.
	 *
	 * @param context
	 */
	public CandidateView(Context context) {
		super(context);
		mSelectionHighlight = context.getResources().getDrawable(
			android.R.drawable.list_selector_background);
		mSelectionHighlight.setState(new int[] {
			android.R.attr.state_enabled, android.R.attr.state_focused,
			android.R.attr.state_window_focused, android.R.attr.state_pressed });

		Resources r = context.getResources();

		setBackgroundColor(r.getColor(R.color.candidate_background));

		mColorNormal = r.getColor(R.color.candidate_normal);
		mColorRecommended = r.getColor(R.color.candidate_recommended);
		mColorOther = r.getColor(R.color.candidate_other);
		mVerticalPadding = r.getDimensionPixelSize(R.dimen.candidate_vertical_padding);

		mPaint = new Paint();
		mPaint.setColor(mColorNormal);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_height));
		mPaint.setStrokeWidth(0);

		mPadding = new Rect();

		setHorizontalFadingEdgeEnabled(true);
		setWillNotDraw(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
	}

	@Override
	public int computeHorizontalScrollRange() {
		return mTotalWidth;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = resolveSize(50, widthMeasureSpec);

		// Get the desired height of the icon menu view (last row of items does
		// not have a divider below)

		mSelectionHighlight.getPadding(mPadding);
		final int desiredHeight = ((int) mPaint.getTextSize()) + mVerticalPadding + mPadding.top
			+ mPadding.bottom;

		// Maximum possible width and desired height
		setMeasuredDimension(measuredWidth, resolveSize(desiredHeight, heightMeasureSpec));
	}

	/**
	 * If the canvas is null, then only touch calculations are performed to pick
	 * the target candidate.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mTotalWidth = 0;
		if (mSuggestions == null)
			return;

		if (mBgPadding == null) {
			mBgPadding = new Rect(0, 0, 0, 0);
			if (getBackground() != null) {
				getBackground().getPadding(mBgPadding);
			}
		}
		int x = 0;
		final int count = mSuggestions.size();
		final int height = getHeight();
		final Rect bgPadding = mBgPadding;
		final Paint paint = mPaint;
		final int y = (int) (((height - mPaint.getTextSize()) / 2) - mPaint.ascent());

		for (int i = 0; i < count; i++) {
			String suggestion = mSuggestions.get(i);
			float textWidth = paint.measureText(suggestion);
			final int wordWidth = (int) textWidth + X_GAP * 2;

			mWordX[i] = x;
			mWordWidth[i] = wordWidth;
			paint.setColor(mColorNormal);
			// if (touchX + scrollX >= x && touchX + scrollX < x + wordWidth &&
			// !scrolled) {
			if (i == mSelectedIndex) {
				canvas.translate(x, 0);
				mSelectionHighlight.setBounds(0, bgPadding.top, wordWidth, height);
				mSelectionHighlight.draw(canvas);
				canvas.translate(-x, 0);
				paint.setFakeBoldText(true);
				paint.setColor(mColorRecommended);
			} else {
				paint.setColor(mColorOther);
			}

			canvas.drawText(suggestion, x + X_GAP, y, paint);
			paint.setColor(mColorOther);
			canvas.drawLine(x + wordWidth + 0.5f, bgPadding.top, x + wordWidth + 0.5f, height + 1,
				paint);
			paint.setFakeBoldText(false);

			x += wordWidth;
		}
		mTotalWidth = x;
		if (mTargetScrollX != getScrollX()) {
			scrollToTarget();
		}
	}

	private void scrollToTarget() {
		int sx = getScrollX();
		if (mTargetScrollX > sx) {
			sx += SCROLL_PIXELS;
			if (sx >= mTargetScrollX) {
				sx = mTargetScrollX;
				requestLayout();
			}
		} else {
			sx -= SCROLL_PIXELS;
			if (sx <= mTargetScrollX) {
				sx = mTargetScrollX;
				requestLayout();
			}
		}
		scrollTo(sx, getScrollY());
		invalidate();
	}

	protected void setSuggestions(List<String> suggestions, int initialSel) {
		clear();
		if (suggestions != null) {
			mSuggestions = suggestions;
			mSelectedIndex = initialSel;
		}
		scrollTo(0, 0);
		mTargetScrollX = 0;
		// Compute the total width
		// onDraw(null);
		invalidate();
		requestLayout();
	}

	protected void clear() {
		mSuggestions = EMPTY_LIST;
		mSelectedIndex = -1;
		invalidate();
	}

	protected void scrollSuggestion(int increment) {
		if (mSuggestions != null && mSuggestions.size() > 1) {
			mSelectedIndex = mSelectedIndex + increment;
			if (mSelectedIndex == mSuggestions.size()) {
				mSelectedIndex = 0;
			} else if (mSelectedIndex < 0) {
				mSelectedIndex = mSuggestions.size() - 1;
			}

			int fullsize = getWidth();
			int halfsize = fullsize / 2;
			mTargetScrollX = mWordX[mSelectedIndex] + (mWordWidth[mSelectedIndex] / 2) - halfsize;
			if (mTargetScrollX < 0) {
				mTargetScrollX = 0;
			} else if (mTargetScrollX > (mTotalWidth - fullsize)) {
				mTargetScrollX = mTotalWidth - fullsize;
			}

			invalidate();
		}
	}

}
