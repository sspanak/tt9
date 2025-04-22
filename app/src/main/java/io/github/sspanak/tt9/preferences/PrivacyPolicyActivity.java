package io.github.sspanak.tt9.preferences;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.ui.DocumentActivity;

public class PrivacyPolicyActivity extends DocumentActivity {
	@Override
	protected BufferedReader getDocumentReader() throws IOException {
		return new PrivacyPolicyFile(this).getReader();
	}
}
