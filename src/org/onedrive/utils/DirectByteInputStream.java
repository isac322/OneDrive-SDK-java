package org.onedrive.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.onedrive.exceptions.InternalException;

import java.io.InputStream;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class DirectByteInputStream extends InputStream {
	@NotNull private static final IndexOutOfBoundsException INDEX_EXCEPTION =
			new IndexOutOfBoundsException();
	protected byte[] buffer;
	@Getter protected int in = -1, out = 0;
	protected int capacity;
	protected boolean closed;

	public DirectByteInputStream() {
		this(1024);
	}

	public DirectByteInputStream(int capacity) {
		if (capacity < 0)
			throw new IllegalArgumentException("Argument capacity's value \"" + capacity + "\" is negative.");
		this.capacity = capacity;
		buffer = new byte[capacity];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(@NotNull byte[] b, int off, int len) {
		if (off < 0 || len < 0 || len > b.length - off) throw INDEX_EXCEPTION;
		else if (len == 0) return 0;

		int end = off + len;
		int m = len < available() ? len : available();

		int v = read();
		if (v == -1) return -1;
		b[off++] = (byte) v;

		for (int i = 1; i < m; i++) {
			b[off++] = buffer[out++];
		}

		for (int i = m; i < end; i++) {
			v = read();
			if (v == -1) {
				return m;
			}
			b[off++] = (byte) m;
		}

		return end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long n) {
		if (n < 0) return 0;

		long dif = n;
		if (n + out > in) {
			out = in;
			dif = in - out;
		}
		else out += n;

		return dif;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() {
		if (in < out) return 0;
		else return in - out + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() {
		while (in < out) {
			if (closed) return -1;
			try {
				wait(300);
			}
			catch (InterruptedException e) {
				throw new InternalException("wait() is wrong.", e);
			}
		}
		return buffer[out++];
	}

	@Override
	public void close() {
		closed = true;
	}

	public void ensureCapacity(int newCapacity) {
		if (newCapacity <= capacity) return;

		int tmp = capacity;
		while (tmp < newCapacity) tmp <<= 1;

		byte[] newBuffer = new byte[tmp];
		System.arraycopy(buffer, 0, newBuffer, 0, capacity);
		capacity = tmp;
		buffer = newBuffer;
	}

	public void ensureRemain(int additional) {
		ensureCapacity(in + additional + 1);
	}

	public byte[] rawBuffer() {
		return buffer;
	}

	public void jumpTo(int newIn) {
		in = newIn;
	}
}
