package io.github.sspanak.tt9.db.sqlite;

class Migration {
	static final Migration[] LIST = {
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN fileHash TEXT NOT NULL DEFAULT 0",
			true
		),
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " RENAME COLUMN  normalizationPending TO _delete_me_0",
			true
		),
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN positionsToNormalize TEXT NULL",
			true
		),
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN maxWordsPerSequence INTEGER NOT NULL DEFAULT -1",
			true
		)
	};

	final String query;
	final boolean mayFail;
	private Migration(String query, boolean mayFail) {
		this.query = query;
		this.mayFail = mayFail;
	}
}
