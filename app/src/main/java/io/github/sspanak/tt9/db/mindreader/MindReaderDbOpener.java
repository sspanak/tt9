package io.github.sspanak.tt9.db.mindreader;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.sqlite.Migration;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.sqlite.Tables;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class MindReaderDbOpener extends SQLiteOpener {
	private static MindReaderDbOpener self;

	private MindReaderDbOpener(Context context) {
		super(context.getApplicationContext(), "tt9.mindreader.db");
	}

	public static MindReaderDbOpener getInstance(Context context) {
		if (self == null) {
			self = new MindReaderDbOpener(context);
		}

		return self;
	}

	@NonNull
	@Override
	protected String[] getCreateQueries() {
		return Tables.getMindReaderCreateQueries(LanguageCollection.getAll());
	}

	@NonNull
	@Override
	protected Migration[] getMigrations() {
		return new Migration[0];
	}
}
