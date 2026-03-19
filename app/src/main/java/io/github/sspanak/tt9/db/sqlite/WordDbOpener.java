package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class WordDbOpener extends SQLiteOpener {
	private static WordDbOpener self;

	private final ArrayList<Language> allLanguages;

	private WordDbOpener(Context context) {
		super(context, "tt9.db");
		allLanguages = new ArrayList<>(LanguageCollection.getAll());
	}

	public static WordDbOpener getInstance(Context context) {
		if (self == null) {
			self = new WordDbOpener(context);
		}

		return self;
	}

	@NonNull
	@Override
	protected String[] getCreateQueries() {
		return Tables.getCreateQueries(allLanguages);
	}

	@NonNull
	@Override
	Migration[] getMigrations() {
		return Migration.WORDS;
	}
}
