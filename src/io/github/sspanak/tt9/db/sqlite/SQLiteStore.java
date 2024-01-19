package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class SQLiteStore extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "tt9.db";
	private static final int DATABASE_VERSION = 1;

	private static final String CUSTOM_WORDS_TABLE = "custom_words_";
	private static final String POSITIONS_TABLE_BASE_NAME = "word_positions_";
	private static final String WORDS_TABLE_BASE_NAME = "words_";

	private final Context context;

	private SQLiteDatabase db;
	final WordOperations ops;

	public SQLiteStore(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		ops = new WordOperations(this);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createCustomWordsTable(db);
		for (Language language : LanguageCollection.getAll(context)) {
			createWordsTable(db, language.getId());
			createWordPositionsTable(db, language.getId());
		}
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
		setWriteAheadLoggingEnabled(true);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// No migrations as of now
	}

	public SQLiteDatabase getDb() {
		if (db == null) {
			db = getWritableDatabase();
		}
		return db;
	}

	static String getCustomWordsTable() { return CUSTOM_WORDS_TABLE; }
	static String getWordsTable(int langId) { return WORDS_TABLE_BASE_NAME + langId; }
	static String getWordPositionsTable(int langId) { return POSITIONS_TABLE_BASE_NAME + langId; }



	private void createWordsTable(SQLiteDatabase db, int langId) {
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + getWordsTable(langId) + " (" +
				"frequency INTEGER NOT NULL DEFAULT 0, " +
				"isCustom TINYINT NOT NULL DEFAULT 0, " +
				"position INTEGER NOT NULL, " +
				"word TEXT NOT NULL" +
			")"
		);
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_position_" + langId + " ON " + getWordsTable(langId) + " (position, word)");
	}

	private void createWordPositionsTable(SQLiteDatabase db, int langId) {
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + getWordPositionsTable(langId) + " (" +
				"sequence TEXT NOT NULL, " +
				"start INTEGER NOT NULL, " +
				"end INTEGER NOT NULL" +
			")"
		);
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_sequence_start_" + langId + " ON " + getWordPositionsTable(langId) + " (sequence, `start`)");
	}

	private void createCustomWordsTable(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + CUSTOM_WORDS_TABLE + " (" +
				"id INTEGER PRIMARY KEY, " +
				"langId INTEGER NOT NULL, " +
				"sequence TEXT NOT NULL, " +
				"word INTEGER NOT NULL " +
			")"
		);

		db.execSQL("CREATE INDEX IF NOT EXISTS idx_custom_words ON " + CUSTOM_WORDS_TABLE + " (langId, sequence)");
	}
}
