package io.github.sspanak.tt9.db.sqlite;

class Migration {
	static final Migration[] LIST = {
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN fileHash TEXT NOT NULL DEFAULT 0"
		),
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " RENAME COLUMN  normalizationPending TO _delete_me_0"
		),
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN positionsToNormalize TEXT NULL"
		),
		new Migration(
			// enforce the new Vietnamese layout
			"DELETE FROM " + Tables.LANGUAGES_META + " WHERE langId = 481590",
			952
		),
		new Migration(
			// DROP COLUMN is supported in SQLite 3.35.0 which comes with API 34+, so...
			"ALTER TABLE " + Tables.LANGUAGES_META + " RENAME COLUMN  maxWordsPerSequence TO _delete_me_1",
			1009
		)
	};

	final String query;
	final int oldVersion;

	private Migration(String query) {
		this.oldVersion = Integer.MAX_VALUE;
		this.query = query;
	}

	private Migration(String query, int oldVersion) {
		this.oldVersion = oldVersion;
		this.query = query;
	}
}
