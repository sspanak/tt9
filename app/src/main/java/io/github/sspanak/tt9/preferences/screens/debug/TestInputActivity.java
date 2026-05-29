package io.github.sspanak.tt9.preferences.screens.debug;

import android.os.Bundle;
import android.widget.LinearLayout;

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
		setContentView(new TestInputBuilder(root).create(this));
	}
}

