package io.github.sspanak.tt9.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HighlightedText {
	private static final String LOG_TAG = HighlightedText.class.getSimpleName();

	private static final int TYPEFACE_UNDEFINED = -1;


	@Nullable private final CharSequence text;
	private final int length;
	private final int typeface;
	private final boolean underline;

	private int regionStart = 0;
	private int regionEnd = 0;
	private int regionTypeface = TYPEFACE_UNDEFINED;
	private boolean regionUnderline = false;


	public HighlightedText(@Nullable CharSequence text, boolean underline, boolean bold) {
		this.text = text;
		this.length = text != null ? text.length() : 0;

		this.typeface = bold ? Typeface.BOLD : Typeface.NORMAL;
		this.underline = underline;
	}


	public HighlightedText setRegion(int start, int end, boolean bold, boolean italic, boolean underline) {
		if (start > end) {
			Logger.w(LOG_TAG, "Not applying extra highlight to invalid text range: [" + start + ", " + end + "]");
			regionTypeface = TYPEFACE_UNDEFINED;
			return this;
		}

		regionStart = Math.max(0, start);
		regionEnd = Math.min(length, end);
		regionUnderline = underline;

		if (bold && italic) {
			regionTypeface = Typeface.BOLD_ITALIC;
		} else if (bold) {
			regionTypeface = Typeface.BOLD;
		} else if (italic) {
			regionTypeface = Typeface.ITALIC;
		} else {
			regionTypeface = Typeface.NORMAL;
		}

		return this;
	}


	public CharSequence highlight() {
		if (text == null || length == 0 || !Character.isLetterOrDigit(text.charAt(0))) {
			return text;
		}

		final SpannableString styledText = new SpannableString(text);

		styledText.setSpan(new StyleSpan(typeface), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		if (underline) {
			styledText.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}

		if (regionTypeface == TYPEFACE_UNDEFINED) {
			return styledText;
		}

		styledText.setSpan(new StyleSpan(regionTypeface), regionStart, regionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		if (regionUnderline) {
			styledText.setSpan(new UnderlineSpan(), regionStart, regionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}

		return styledText;
	}


	@NonNull
	@Override
	public String toString() {
		return text == null ? "" : text.toString();
	}
}
