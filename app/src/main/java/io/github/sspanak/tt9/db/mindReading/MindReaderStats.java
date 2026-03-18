package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

public class MindReaderStats {
	private final MindReader mindReader;

	private static long countComplete = 0;
	private static long countGuess = 0;
	private static long countSetContext = 0;
	private static long countSetLanguage = 0;

	private static long totalComplete = 0;
	private static long totalGuess = 0;
	private static long totalSetContext = 0;
	private static long totalSetLanguage = 0;

	private static long slowestComplete = 0;
	private static long slowestGuess = 0;
	private static long slowestSetContext = 0;
	private static long slowestSetLanguage = 0;
	@NonNull private static String statsSnapshot = "";


	MindReaderStats(@NonNull MindReader reader) {
		mindReader = reader;
	}


	MindReaderStats clear() {
		slowestComplete = slowestGuess = slowestSetContext = slowestSetLanguage = 0;
		return this;
	}


	void recordCompleteTime(long time) {
		countComplete++;
		totalComplete += time;
		slowestComplete = Math.max(slowestComplete, time);
	}


	void recordGuessTime(long time) {
		countGuess++;
		totalGuess += time;
		slowestGuess = Math.max(slowestGuess, time);
	}


	void recordSetContextTime(long time) {
		countSetContext++;
		totalSetContext += time;
		slowestSetContext = Math.max(slowestSetContext, time);
	}


	void recordSetLanguageTime(long time) {
		countSetLanguage++;
		totalSetLanguage += time;
		slowestSetLanguage = Math.max(slowestSetLanguage, time);
	}


	public void update() {
		StringBuilder sb = new StringBuilder();

		sb.append("Status").append(mindReader.isOff() ? ": Off\n" : ": On\n");
		sb.append("Language: ").append(mindReader.wordContext.language).append("\n");
		sb.append("Dictionary size: ").append(mindReader.dictionary.size()).append(" tokens\n");
		sb.append("N-grams: ").append(mindReader.ngrams.size()).append(" / ").append(mindReader.ngrams.capacity()).append("\n");

		if (!mindReader.isOff()) {
			sb.append("\nComplete word: ").append(countComplete)
				.append(".  Average: ").append(countComplete == 0 ? 0 : totalComplete / countComplete)
				.append(" ms.  Slowest: ")
				.append(slowestComplete).append(" ms\n");
			sb.append("Guess word: ").append(countGuess)
				.append(".  Average: ").append(countGuess == 0 ? 0 : totalGuess / countGuess)
				.append(" ms.  Slowest: ")
				.append(slowestGuess).append(" ms\n");
			sb.append("Set context: ").append(countSetContext)
				.append(".  Average: ").append(countSetContext == 0 ? 0 : totalSetContext / countSetContext)
				.append(" ms.  Slowest: ")
				.append(slowestSetContext).append(" ms\n");
			sb.append("Set language: ").append(countSetLanguage)
				.append(".  Average: ").append(countSetLanguage == 0 ? 0 : totalSetLanguage / countSetLanguage)
				.append(" ms.  Slowest: ")
				.append(slowestSetLanguage).append(" ms\n");
		}

		statsSnapshot = sb.toString();
	}


	@NonNull
	public static String get() {
		return statsSnapshot.isEmpty() ? "No mind read." : statsSnapshot;
	}
}
