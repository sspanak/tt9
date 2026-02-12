package io.github.sspanak.tt9.preferences.screens.debug;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import io.github.sspanak.tt9.ui.EdgeToEdgeActivity;

public class TestInputActivity extends EdgeToEdgeActivity {
	private LinearLayout root;


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		preventEdgeToEdge(root);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setFitsSystemWindows(true);
		root.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT
		));

		// Root ScrollView (useful when testing focus & keyboard behavior)
		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(root);
		scrollView.setPadding(32, 32, 32, 32);

		addField(root, "Number (unspecified)", InputType.TYPE_CLASS_NUMBER);
		addField(root, "Number (signed integer)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		addField(root, "Number (unsigned decimal)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		addField(root, "Number (signed decimal)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
		addField(root, "Number (password)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		addField(root, "Number (phone)", InputType.TYPE_CLASS_PHONE);
		addField(root, "Text (unspecified)", InputType.TYPE_CLASS_TEXT);
		addField(root, "Text (email)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		addField(root, "Text (password)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		addField(root, "Text (url)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

		setContentView(scrollView);
	}


	private void addField(LinearLayout parent, String label, int inputType) {
		TextView textView = new TextView(this);
		textView.setText(label);

		EditText editText = new EditText(this);
		editText.setInputType(inputType);
		editText.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		));
		editText.setSingleLine(true);

		parent.addView(textView);
		parent.addView(editText);
	}
}

