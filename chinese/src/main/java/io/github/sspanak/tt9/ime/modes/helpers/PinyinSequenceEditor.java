package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.Collection;
import java.util.Locale;

/** A cursor over T9 digits, retaining the selected reading wherever possible. */
public final class PinyinSequenceEditor {
	private String digits;
	private String reading;
	private String resolvedReading;
	private int cursor;
	private boolean readingConfirmed = true;


	public PinyinSequenceEditor(String digits, String reading) {
		this.digits = digits;
		this.reading = reading.toUpperCase(Locale.ROOT);
		resolvedReading = this.reading;
		if (!matchesDigits(this.reading, digits)) {
			throw new IllegalArgumentException("The reading must match the phonetic key sequence");
		}
		cursor = digits.length();
	}


	public synchronized String getDigits() { return digits; }
	public synchronized String getReading() { return reading; }
	public synchronized int getCursor() { return cursor; }
	public synchronized String getPreview() { return reading.substring(0, cursor) + "|" + reading.substring(cursor); }
	public synchronized boolean hasResolvedReading() { return readingConfirmed && !reading.isEmpty() && matchesDigits(reading, digits); }
	public synchronized String getResolvedReading() { return hasResolvedReading() ? resolvedReading : ""; }


	public synchronized void moveCursor(int direction) {
		setCursor(cursor + Integer.signum(direction));
	}

	public synchronized void setCursor(int offset) {
		cursor = Math.max(0, Math.min(offset, digits.length()));
	}


	public synchronized boolean insertDigit(int digit) {
		if (digit < 2 || digit > 9) {
			return false;
		}
		digits = digits.substring(0, cursor) + digit + digits.substring(cursor);
		reading = reading.substring(0, cursor) + digit + reading.substring(cursor);
		cursor++;
		readingConfirmed = false;
		return true;
	}


	public synchronized boolean deleteBeforeCursor() {
		if (cursor == 0) {
			return false;
		}
		digits = digits.substring(0, cursor - 1) + digits.substring(cursor);
		reading = reading.substring(0, cursor - 1) + reading.substring(cursor);
		cursor--;
		readingConfirmed = false;
		return true;
	}


	/** Resolve ambiguous inserted digits using readings that preserve the other selected letters. */
	public synchronized void resolveReading(Collection<String> candidates) {
		String best = null;
		String bestReading = "";
		int bestScore = -1;
		for (String candidate : candidates) {
			String normalized = candidate.toUpperCase(Locale.ROOT).replace("'", "");
			if (!matchesDigits(normalized, digits)) {
				continue; // Ignore stale query results and longer completions.
			}
			int score = 0;
			for (int i = 0; i < reading.length(); i++) {
				if (reading.charAt(i) == normalized.charAt(i)) {
					score++;
				}
			}
			if (score > bestScore) {
				best = normalized;
				bestReading = candidate.toUpperCase(Locale.ROOT);
				bestScore = score;
			}
		}
		if (best != null) {
			reading = best;
		}
		readingConfirmed = best != null;
		resolvedReading = bestReading;
	}


	private static boolean matchesDigits(String reading, String digits) {
		if (reading.length() != digits.length()) {
			return false;
		}
		final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String keyMap = "22233344455566677778889999";
		for (int i = 0; i < reading.length(); i++) {
			int letter = alphabet.indexOf(reading.charAt(i));
			if (letter < 0 || digits.charAt(i) != keyMap.charAt(letter)) {
				return false;
			}
		}
		return true;
	}
}
