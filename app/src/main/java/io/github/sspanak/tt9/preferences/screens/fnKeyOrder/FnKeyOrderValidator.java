package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.R;

public class FnKeyOrderValidator {
	public final static int ERROR_SIDE_LEFT = -1;
	public final static int ERROR_SIDE_RIGHT = 1;
	public final static int ERROR_SIDE_BOTH = 0;

	private Integer error;
	private int errorSide = ERROR_SIDE_BOTH;

	private final String left;
	private final String right;


	public FnKeyOrderValidator(String left, String right) {
		this.left = left;
		this.right = right;
	}


	public Integer getError() {
		return error;
	}


	public int getErrorSide() {
		return errorSide;
	}


	public boolean validate() {
		error = null;
		errorSide = ERROR_SIDE_BOTH;

		return
			validateLength(left, ERROR_SIDE_LEFT) && validateLength(right, ERROR_SIDE_RIGHT)
			&& validateDigits(left, ERROR_SIDE_LEFT) && validateDigits(right, ERROR_SIDE_RIGHT)
			&& validateNoRepeat(left, ERROR_SIDE_LEFT) && validateNoRepeat(right, ERROR_SIDE_RIGHT)
			&& validateNoOverlap(left, right);
	}


	private boolean validateLength(String text, int side) {
		if (text == null || text.length() > 4) {
			error = R.string.fn_key_order_error_wrong_key_count;
			errorSide = side;
			return false;
		}

		return true;
	}


	private boolean validateDigits(String text, int side) {
		if (text == null || !text.matches("^[1-8CcEeLlNnRrTtVv]*$")) {
			error = R.string.fn_key_order_error_unsupported_key_code;
			errorSide = side;
			return false;
		}

		return true;
	}


	private boolean validateNoRepeat(String text, int side) {
		if (text == null) {
			return true;
		}

		Set<Character> keyCodes = new HashSet<>();

		for (char c : text.toUpperCase().toCharArray()) {
			if (keyCodes.contains(c)) {
				error = R.string.fn_key_order_error_duplicate_key;
				errorSide = side;
				return false;
			}
			keyCodes.add(c);
		}

		return true;
	}


	private boolean validateNoOverlap(String column, String otherColumn) {
		if (column == null || otherColumn == null) {
			return true;
		}

		String columnUpper = column.toUpperCase();
		String otherColumnUpper = otherColumn.toUpperCase();

		for (int i = 0; i < columnUpper.length(); i++) {
			char c = columnUpper.charAt(i);
			if (otherColumnUpper.indexOf(c) != -1) {
				error = R.string.fn_key_order_error_key_on_both_sides;
				errorSide = ERROR_SIDE_BOTH;
				return false;
			}
		}

		return true;
	}
}
