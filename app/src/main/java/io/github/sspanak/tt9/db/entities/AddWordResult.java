package io.github.sspanak.tt9.db.entities;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;

public class AddWordResult {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_BLANK_WORD = 1;
	public static final int CODE_INVALID_LANGUAGE = 2;
	public static final int CODE_WORD_EXISTS = 3;
	public static final int CODE_GENERAL_ERROR = 666;

	private final int statusCode;
	private final String word;

	public AddWordResult(int statusCode, String word) {
		this.statusCode = statusCode;
		this.word = word;
	}

	public String toHumanFriendlyString(Context context) {
		switch (statusCode) {
			case CODE_SUCCESS:
				return context.getString(R.string.add_word_success, word);

			case CODE_WORD_EXISTS:
				return context.getResources().getString(R.string.add_word_exist, word);

			case CODE_BLANK_WORD:
				return context.getString(R.string.add_word_blank);

			case CODE_INVALID_LANGUAGE:
				return context.getResources().getString(R.string.add_word_invalid_language);

			default:
				return context.getString(R.string.error_unexpected);
			}
	}

	@NonNull
	@Override
	public String toString() {
		switch (statusCode) {
			case CODE_SUCCESS:
				return "Success";

			case CODE_BLANK_WORD:
				return "Blank word";

			case CODE_INVALID_LANGUAGE:
				return "Invalid language";

			case CODE_WORD_EXISTS:
				return "Word '" + word + "' exists";

			case CODE_GENERAL_ERROR:
				return "General error";

			default:
				return "Unknown error";
		}
	}
}
