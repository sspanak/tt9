package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.LanguageCollection;

public class WordDbOpener extends SQLiteOpener {
	private static WordDbOpener self;

	private WordDbOpener(Context context) {
		super(context.getApplicationContext(), "tt9.db");
	}

	@NonNull
	public static WordDbOpener getInstance(Context context) {
		if (self == null) {
			self = new WordDbOpener(context);
		}

		return self;
	}

	@NonNull
	@Override
	protected String[] getCreateQueries() {
		return Tables.getWordsCreateQueries(LanguageCollection.getAll());
	}

	@NonNull
	@Override
	protected Migration[] getMigrations() {
		return Migration.WORDS;
	}
}
