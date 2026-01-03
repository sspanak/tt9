package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class FnKeyOrderScreen extends BaseScreenFragment {
	final public static String NAME = "FnKeyOrder";

	private LeftFnOrderPreference left;
	private RightFnOrderPreference right;

	public FnKeyOrderScreen() { super(); }
	public FnKeyOrderScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_fn_key_order; }
	@Override protected int getXml() { return R.xml.prefs_screen_fn_key_order; }

	@Override
	protected void onCreate() {
		createTextFields();
		(new ItemResetFnKeyOrder(findPreference(ItemResetFnKeyOrder.NAME), left, right)).enableClickHandler();
		resetFontSize(true);
	}

	private void createTextFields() {
		left = findPreference(LeftFnOrderPreference.NAME);
		right = findPreference(RightFnOrderPreference.NAME);
		if (left != null && right != null) {
			left.setTextChangeHandler(this::onTextChange);
			right.setTextChangeHandler(this::onTextChange);
		}
	}

	private void onTextChange() {
		if (left == null || right == null || activity == null) {
			return;
		}

		FnKeyOrderValidator validator = activity.getSettings().setFnKeyOrder(left.getText().toString(), right.getText().toString());
		left.setError("");
		right.setError("");

		if (validator.getError() == null) {
			return;
		}

		if (validator.getErrorSide() == FnKeyOrderValidator.ERROR_SIDE_LEFT) {
			left.setError(getString(validator.getError()));
		} else if (validator.getErrorSide() == FnKeyOrderValidator.ERROR_SIDE_RIGHT) {
			right.setError(getString(validator.getError()));
		} else if (validator.getErrorSide() == FnKeyOrderValidator.ERROR_SIDE_BOTH) {
			left.setError(getString(validator.getError()));
			right.setError(getString(validator.getError()));
		}
	}
}
