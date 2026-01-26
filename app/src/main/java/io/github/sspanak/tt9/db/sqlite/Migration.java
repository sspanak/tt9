package io.github.sspanak.tt9.db.sqlite;

import io.github.sspanak.tt9.languages.EmojiLanguage;

record Migration(String query, int oldVersion) {
	static final Migration[] LIST = {
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN fileHash TEXT NOT NULL DEFAULT 0",
			827
		),
		new Migration(
			// DROP COLUMN is supported in SQLite 3.35.0 which comes with API 34+, so...
			"ALTER TABLE " + Tables.LANGUAGES_META + " RENAME COLUMN  normalizationPending TO _delete_me_0",
			827
		),
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN positionsToNormalize TEXT NULL",
			827
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
		),
		new Migration(
			// fix custom emoji with an incorrect sequence accidentally caused when introducing Sequences()
			"DELETE FROM " + Tables.CUSTOM_WORDS + " WHERE langId = " + new EmojiLanguage(null).getId(),
			1287
		)
	};
}
