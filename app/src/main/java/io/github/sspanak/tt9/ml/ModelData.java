package io.github.sspanak.tt9.ml;

/**
 * Represents a Whisper model configuration
 */
public class ModelData {
	public final String name;
	public final boolean isBuiltinAsset;
	public final String ggmlFile;
	public final String digest;

	public ModelData(String name, boolean isBuiltinAsset, String ggmlFile, String digest) {
		this.name = name;
		this.isBuiltinAsset = isBuiltinAsset;
		this.ggmlFile = ggmlFile;
		this.digest = digest;
	}
}
