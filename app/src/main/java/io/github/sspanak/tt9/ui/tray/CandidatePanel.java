package io.github.sspanak.tt9.ui.tray;

import android.graphics.drawable.Drawable;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Optional candidate presentation; the ordinary row works without an implementation. */
public interface CandidatePanel {
	interface Host {
		RecyclerView view();
		SuggestionsAdapter adapter();
		List<String> candidates();
		int selection();
		void select(int index, boolean scroll);
		void showAll();
		void render();
		void cancelPendingScroll();
		void updateSeparators();
		void setExtraHeight(int height);
	}

	boolean canExpand();
	boolean expand();
	boolean collapse();
	boolean isExpanded();
	boolean usesNumberKeys();
	int numberedIndex(int number);
	void move(int horizontal, int vertical, boolean collapseAtTop);
	RecyclerView.ItemDecoration separators(Drawable divider);
}
