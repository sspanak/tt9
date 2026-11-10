package io.github.sspanak.tt9.chinese;

import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.tray.CandidatePanel;
import io.github.sspanak.tt9.ui.tray.SuggestionsAdapter;

/** Chinese-only expanded layout, row shortcuts and navigation, delivered in the optional split. */
final class ChineseExpandedCandidates implements CandidatePanel, SuggestionsAdapter.Presentation {
	private final SettingsStore settings;
	private final Host host;
	private PackedCandidateNavigation navigation;
	private CandidateNumbering numbering;
	private int collapsedHeight;
	private int ordinaryRowHeight;
	private int ordinaryWrapperHeight;
	private int rowHeight;
	private int padding;
	private int separatorWidth;
	private float baseTextSize;

	ChineseExpandedCandidates(SettingsStore settings, Host host) { this.settings = settings; this.host = host; }

	@Override public boolean canExpand() {
		if (isExpanded() || host.view().getVisibility() != View.VISIBLE || host.candidates().size() < 2) return false;
		int index = Math.max(0, Math.min(host.selection(), host.candidates().size() - 1));
		return host.candidates().get(index).codePoints().anyMatch(code -> Character.UnicodeScript.of(code) == Character.UnicodeScript.HAN);
	}

	@Override public boolean isExpanded() { return navigation != null; }
	@Override public boolean usesNumberKeys() { return isExpanded() && numbering != null; }

	@Override public boolean expand() {
		if (!canExpand()) return false;
		RecyclerView view = host.view();
		float density = view.getResources().getDisplayMetrics().density;
		int width = Math.max(1, view.getWidth() - view.getPaddingLeft() - view.getPaddingRight());
		boolean roomy = ChineseCandidateSettings.getCandidateRoomy(settings);
		baseTextSize = view.getResources().getDimension(R.dimen.status_bar_text_size);
		float textSize = baseTextSize * settings.getSuggestionFontScale();
		padding = view.getResources().getDimensionPixelSize(R.dimen.main_small_suggestion_spacing_horizontal) + (roomy ? Math.round(2 * density) : 0);
		TextPaint paint = new TextPaint();
		paint.setTextSize(textSize);
		int ordinaryHeight = collapsedRowHeight(view, paint);
		rowHeight = roomy ? (int) Math.ceil(ordinaryHeight * 1.15) : ordinaryHeight;
		host.showAll();
		boolean numbered = ChineseCandidateSettings.getNumberSelect(settings);
		int numberWidth = numbered ? (int) Math.ceil(paint.measureText("9 ") * 0.65f) : 0;
		separatorWidth = Math.round(density);
		int[] widths = new int[host.candidates().size()];
		for (int i = 0; i < widths.length; i++) {
			widths[i] = (int) Math.ceil(paint.measureText(host.candidates().get(i))) + numberWidth + padding * 2 + separatorWidth;
		}
		navigation = new PackedCandidateNavigation(widths, width, numbered ? 9 : 0);
		numbering = numbered ? new CandidateNumbering(navigation) : null;
		int maxRows = Math.max(1, (int) (view.getResources().getDisplayMetrics().heightPixels * 0.4f / rowHeight));
		int rows = Math.min(navigation.getRowCount(), Math.min(6, maxRows));
		int height = rowHeight * rows;
		View wrapper = (View) view.getParent();
		ViewGroup.LayoutParams params = wrapper.getLayoutParams();
		collapsedHeight = params.height;
		int normalWrapperHeight = params.height > 0 ? params.height
			: ordinaryWrapperHeight > 0 ? ordinaryWrapperHeight : ordinaryHeight;
		int extraHeight = Math.max(0, height - normalWrapperHeight);
		params.height = height;
		wrapper.setLayoutParams(params);
		host.cancelPendingScroll();
		host.adapter().setPresentation(this);
		GridLayoutManager layout = new GridLayoutManager(view.getContext(), width);
		layout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override public int getSpanSize(int position) { return navigation != null ? navigation.getSpanSize(position) : 1; }
		});
		view.setLayoutManager(layout);
		view.setVerticalScrollBarEnabled(true);
		host.updateSeparators();
		host.setExtraHeight(extraHeight);
		host.render();
		view.scrollToPosition(host.selection());
		return true;
	}

	private int collapsedRowHeight(RecyclerView view, TextPaint paint) {
		// Capture the ordinary row before changing the adapter or its layout manager.
		View item = view.getChildCount() > 0 ? view.getChildAt(0) : null;
		boolean ordinary = item != null && view.getChildViewHolder(item).getItemViewType() == 0;
		if (ordinary && item.getHeight() > 0) {
			ordinaryRowHeight = item.getHeight();
			ordinaryWrapperHeight = ((View) view.getParent()).getHeight();
		}
		// Old grid holders can survive a rapid collapse/reopen until the next layout.
		if (ordinaryRowHeight > 0) return ordinaryRowHeight;
		int padding = view.getPaddingTop() + view.getPaddingBottom();
		int height = 0;
		if (ordinary || (item == null && !(view.getLayoutManager() instanceof GridLayoutManager))) {
			height = (view.getHeight() > padding ? view.getHeight() : view.getMeasuredHeight()) - padding;
		}
		if (height <= 0) {
			Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
			height = settings.isMainLayoutNumpad() ? view.getResources().getDimensionPixelSize(R.dimen.numpad_suggestion_height)
				: Math.max(1, metrics.bottom - metrics.top);
		}
		ordinaryRowHeight = height;
		View wrapper = (View) view.getParent();
		int fixedHeight = wrapper.getLayoutParams().height;
		ordinaryWrapperHeight = fixedHeight > 0 ? fixedHeight
			: height + padding + wrapper.getPaddingTop() + wrapper.getPaddingBottom();
		return ordinaryRowHeight;
	}

	@Override public boolean collapse() {
		if (!isExpanded()) return false;
		RecyclerView view = host.view();
		navigation = null;
		numbering = null;
		host.cancelPendingScroll();
		View wrapper = (View) view.getParent();
		ViewGroup.LayoutParams params = wrapper.getLayoutParams();
		params.height = collapsedHeight;
		wrapper.setLayoutParams(params);
		host.adapter().setPresentation(null);
		view.setLayoutManager(new LinearLayoutManager(view.getContext(), RecyclerView.HORIZONTAL, false));
		view.setVerticalScrollBarEnabled(false);
		host.updateSeparators();
		host.setExtraHeight(0);
		// Keep the full candidate list and selection when returning to the ordinary row.
		host.render();
		return true;
	}

	@Override public int numberedIndex(int number) { return usesNumberKeys() ? numbering.getIndex(number, host.selection()) : -1; }

	@Override public void move(int horizontal, int vertical, boolean collapseAtTop) {
		if (!isExpanded()) return;
		if (collapseAtTop && vertical < 0 && navigation.getRow(host.selection()) == 0) { collapse(); return; }
		host.select(navigation.move(host.selection(), horizontal, vertical), true);
	}

	@Override public RecyclerView.ItemDecoration separators(Drawable divider) {
		return isExpanded() ? new CandidateSeparators(divider) : null;
	}

	@Override public int viewType() { return 1; }

	@Override public void bind(View item, TextView text, int position) {
		if (!isExpanded()) return;
		// Retain GridLayoutManager's span metadata when rebinding an existing holder.
		ViewGroup.LayoutParams params = item.getLayoutParams();
		params.width = Math.max(1, navigation.getItemWidth(position) - separatorWidth);
		params.height = rowHeight;
		item.setLayoutParams(params);
		text.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
		text.setMinimumWidth(0);
		text.setPadding(padding, 0, padding, 0);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize);
		text.setSingleLine(true);
		text.setEllipsize(TextUtils.TruncateAt.END);
	}

	@Override public CharSequence text(String candidate, int position) {
		int number = numbering != null ? numbering.getNumber(position) : 0;
		if (number <= 0) return candidate;
		String prefix = number + " ";
		SpannableString result = new SpannableString(prefix + candidate);
		result.setSpan(new RelativeSizeSpan(0.65f), 0, prefix.length(), 0);
		return result;
	}
}
