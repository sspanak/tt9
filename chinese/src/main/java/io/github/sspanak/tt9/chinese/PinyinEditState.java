package io.github.sspanak.tt9.chinese;

/** The edit preview's short lifetime and a finish request waiting for a reading lookup. */
final class PinyinEditState {
	private String preview = "";
	private long revision;
	private long readyRevision = -1;
	private boolean returning;
	private boolean spellingsPending;
	private boolean queryPending;
	private boolean finishPending;

	void publish(String preview) { this.preview = preview; revision++; readyRevision = -1; }
	boolean isPreview(String word) { return !preview.isEmpty() && preview.equals(word); }
	void beginQuery() { queryPending = true; }
	boolean isQueryPending() { return queryPending; }
	boolean isFinishPending() { return finishPending; }
	boolean isTransitionPending() { return returning || spellingsPending || finishPending; }
	boolean isReturning() { return returning; }
	boolean isSpellingsPending() { return spellingsPending; }
	long getRevision() { return revision; }
	long getReadyRevision() { return readyRevision; }

	boolean requestFinish(boolean hasDigits, boolean resolved) {
		if (!hasDigits || resolved) { finishPending = false; return true; }
		finishPending = queryPending;
		return false;
	}

	boolean completeQuery(boolean resolved) {
		queryPending = false;
		boolean finish = finishPending && resolved;
		finishPending = false;
		return finish;
	}

	void beginReturn() {
		revision++; readyRevision = -1;
		returning = true;
		spellingsPending = queryPending = finishPending = false;
	}
	void beginSpellings() {
		revision++; readyRevision = -1;
		spellingsPending = true;
	}
	void predictionsReady() { readyRevision = revision; }
	void suggestionsShown(long renderedRevision) {
		if (renderedRevision == revision && renderedRevision == readyRevision) {
			preview = ""; readyRevision = -1; returning = spellingsPending = false;
		}
	}
	void reset() {
		preview = ""; revision++; readyRevision = -1;
		returning = spellingsPending = queryPending = finishPending = false;
	}
}
