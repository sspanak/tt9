package io.github.sspanak.tt9.preferences.screens.debug;

import android.app.Activity;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

class TestInputBuilder {
	@NonNull private final LinearLayout root;
	ArrayList<EditText> fields = new ArrayList<>();


	TestInputBuilder(@NonNull LinearLayout root) {
		this.root = root;
	}


	ScrollView create(@NonNull Activity activity) {
		root.setOrientation(LinearLayout.VERTICAL);
		root.setFitsSystemWindows(true);
		root.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT
		));

		// Root ScrollView (useful when testing focus & keyboard behavior)
		ScrollView scrollView = new ScrollView(activity);
		scrollView.addView(root);
		scrollView.setPadding(32, 32, 32, 32);

		addField(activity, "Number (unspecified)", InputType.TYPE_CLASS_NUMBER);
		addField(activity, "Number (signed integer)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		addField(activity, "Number (unsigned decimal)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		addField(activity, "Number (signed decimal)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
		addField(activity, "Number (password)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		addField(activity, "Number (phone)", InputType.TYPE_CLASS_PHONE);
		addField(activity, "Text (unspecified)", InputType.TYPE_CLASS_TEXT);
		addField(activity, "Text (multiline)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		addField(activity, "Text (email)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		addField(activity, "Text (password)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		addField(activity, "Text (url)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

		return scrollView;
	}


	private void addField(@NonNull Activity activity, @NonNull String label, int inputType) {
		TextView textView = new TextView(activity);
		textView.setText(label);

		EditText editText = new EditText(activity);
		editText.setInputType(inputType);
		editText.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		));

		final boolean isNumber = (inputType & InputType.TYPE_CLASS_NUMBER) != 0;
		final boolean isTextUnspecified = inputType == InputType.TYPE_CLASS_TEXT;
		final boolean isTextMultiLine = (inputType & (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)) == 0;
		editText.setSingleLine(isNumber || isTextUnspecified || isTextMultiLine);

		root.addView(textView);
		root.addView(editText);
		fields.add(editText);
	}
}
