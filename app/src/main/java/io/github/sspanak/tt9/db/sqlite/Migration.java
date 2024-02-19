package io.github.sspanak.tt9.db.sqlite;

class Migration {
	static final Migration[] LIST = {
		new Migration(
			"ALTER TABLE " + Tables.LANGUAGES_META + " ADD COLUMN updatedAt INTEGER DEFAULT 0",
			true
		)
	};

	String query;
	boolean mayFail;
	private Migration(String query, boolean mayFail) {
		this.query = query;
		this.mayFail = mayFail;
	}
}
