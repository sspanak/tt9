package io.github.sspanak.tt9.util;

import java.io.IOException;
import java.io.OutputStream;

public class LimitedOutputStream extends OutputStream {
	private final OutputStream delegate;
	private final long maxSize;
	private long written = 0;

	public LimitedOutputStream(OutputStream delegate, long maxSize) {
		this.delegate = delegate;
		this.maxSize = maxSize;
	}

	@Override
	public void write(int b) throws IOException {
		checkLimit(1);
		delegate.write(b);
		written++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkLimit(len);
		delegate.write(b, off, len);
		written += len;
	}

	private void checkLimit(int len) throws IOException {
		if (written + len > maxSize) {
			throw new IOException("Decompressed data exceeds maximum allowed size");
		}
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
