package io.github.sspanak.tt9.ime.helpers;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import io.github.sspanak.tt9.Logger;

public class TextHelper {
		public static CharSequence highlightComposingText(CharSequence word, int start, int end, boolean highlightMore) {
		if (end < start || start < 0) {
			Logger.w("tt9.util.highlightComposingText", "Cannot highlight invalid composing text range: [" + start + ", " + end + "]");
			return word;
		}

		SpannableString styledWord = new SpannableString(word);

		// default underline style
		styledWord.setSpan(new UnderlineSpan(), 0, word.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// highlight the requested range
		styledWord.setSpan(
			new StyleSpan(Typeface.BOLD),
			start,
			Math.min(word.length(), end),
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		);

		if (highlightMore) {
			styledWord.setSpan(
				new StyleSpan(Typeface.BOLD_ITALIC),
				start,
				Math.min(word.length(), end),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			);
		}

		return styledWord;
	}
}
