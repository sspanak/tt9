package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

/**
 * Implemented in the "premium" source set. The open-source version
 * has no customizable keys, so this class is empty.
 */
public class BaseSoftKeyCustomizable extends SoftKey {
	public BaseSoftKeyCustomizable(Context context) { super(context); }
	public BaseSoftKeyCustomizable(Context context, AttributeSet attrs) { super(context, attrs); }
	public BaseSoftKeyCustomizable(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	protected ColorStateList getCentralIconColor() { return null; }
}
