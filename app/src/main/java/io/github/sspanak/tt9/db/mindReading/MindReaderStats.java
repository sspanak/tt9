package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MindReaderStats {
	private static final MindReaderStats self = new MindReaderStats();

	private boolean isOff = true;
	private int dictionarySize = 0;
	private int ngramsSize = 0;
	private int ngramsCapacity = 0;
	@Nullable private String language = null;

	private long countComplete = 0;
	private long countGuess = 0;
	private long countSetContext = 0;
	private long countSetLanguage = 0;

	private long totalComplete = 0;
	private long totalGuess = 0;
	private long totalSetContext = 0;
	private long totalSetLanguage = 0;

	private long slowestComplete = 0;
	private long slowestGuess = 0;
	private long slowestSetContext = 0;
	private long slowestSetLanguage = 0;


	@NonNull
	public static MindReaderStats getInstance() {
		return self;
	}


	@NonNull
	public static String get() {
		return getInstance().generate();
	}


	MindReaderStats update(@NonNull MindReader mindReader) {
		isOff = mindReader.isOff();
		dictionarySize = mindReader.dictionary.size();
		ngramsSize = mindReader.ngrams.size();
		ngramsCapacity = mindReader.ngrams.capacity();
		language = mindReader.wordContext.language != null ? mindReader.wordContext.language.toString() : null;

		return this;
	}


	void resetTimings() {
		countComplete = countGuess = countSetContext = countSetLanguage = 0;
		totalComplete = totalGuess = totalSetContext = totalSetLanguage = 0;
		slowestComplete = slowestGuess = slowestSetContext = slowestSetLanguage = 0;
	}


	void setOff(boolean off) {
		isOff = off;
	}


	void setCompleteTime(long time) {
		countComplete++;
		totalComplete += time;
		slowestComplete = Math.max(slowestComplete, time);
	}


	void setGuessTime(long time) {
		countGuess++;
		totalGuess += time;
		slowestGuess = Math.max(slowestGuess, time);
	}


	void setChangeContextTime(long time) {
		countSetContext++;
		totalSetContext += time;
		slowestSetContext = Math.max(slowestSetContext, time);
	}


	void setChangeLanguageTime(long time) {
		countSetLanguage++;
		totalSetLanguage += time;
		slowestSetLanguage = Math.max(slowestSetLanguage, time);
	}


	@NonNull
	private String generate() {
		final StringBuilder sb = new StringBuilder();

		sb.append("Status").append(isOff ? ": Off\n" : ": On\n");
		sb.append("Language: ").append(language).append("\n");
		sb.append("Dictionary size: ").append(dictionarySize).append(" tokens\n");
		sb.append("N-grams: ").append(ngramsSize).append(" / ").append(ngramsCapacity).append("\n");

		if (!isOff) {
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

		return sb.toString();
	}
}
