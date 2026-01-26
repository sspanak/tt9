package io.github.sspanak.tt9.db.entities;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;

public record AddWordResult(int statusCode, String word) {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_BLANK_WORD = 1;
	public static final int CODE_INVALID_LANGUAGE = 2;
	public static final int CODE_WORD_EXISTS = 3;
	public static final int CODE_GENERAL_ERROR = 666;

	public String toHumanFriendlyString(Context context) {
		return switch (statusCode) {
			case CODE_SUCCESS -> context.getString(R.string.add_word_success, word);
			case CODE_WORD_EXISTS -> context.getResources().getString(R.string.add_word_exist, word);
			case CODE_BLANK_WORD -> context.getString(R.string.add_word_blank);
			case CODE_INVALID_LANGUAGE -> context.getResources().getString(R.string.add_word_invalid_language);
			default -> context.getString(R.string.error_unexpected);
		};
	}

	@NonNull
	@Override
	public String toString() {
		return switch (statusCode) {
			case CODE_SUCCESS -> "Success";
			case CODE_BLANK_WORD -> "Blank word";
			case CODE_INVALID_LANGUAGE -> "Invalid language";
			case CODE_WORD_EXISTS -> "Word '" + word + "' exists";
			case CODE_GENERAL_ERROR -> "General error";
			default -> "Unknown error";
		};
	}
}
