package com.bhyoo.onedrive.utils;

import com.bhyoo.onedrive.exceptions.InternalException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static com.bhyoo.onedrive.utils.DirectByteInputStream.INDEX_EXCEPTION;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ByteBufStream extends InputStream {
	@NotNull final private CompositeByteBuf compositeBuf;
	private boolean noMoreBuf;
	private boolean closed;


	public ByteBufStream() {
		this(PooledByteBufAllocator.DEFAULT.compositeBuffer());
	}

	public ByteBufStream(@NotNull CompositeByteBuf byteBuf) {
		compositeBuf = byteBuf;
	}


	public synchronized void setNoMoreBuf() {
		noMoreBuf = true;
		notifyAll();
	}


	/**
	 * Closes this input stream and releases any system resources associated with the stream.
	 */
	@Override public synchronized void close() {
		setNoMoreBuf();
		closed = true;
		compositeBuf.release();
		notifyAll();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int read() {
		while (!closed && !noMoreBuf && !compositeBuf.isReadable()) {
			try {
				wait();
			}
			catch (InterruptedException e) {
				throw new InternalException("wait() is wrong in " + this.getClass().getName() + ".", e);
			}
		}

		return !closed && compositeBuf.isReadable() ? compositeBuf.readByte() : -1;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int read(@NotNull byte[] b) {
		return read(b, 0, b.length);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized int read(@NotNull byte[] b, int off, int len) {
		if (off < 0 || len < 0 || len > b.length - off) throw INDEX_EXCEPTION;
		else if (len == 0) return 0;
		else if (closed) return -1;

		final int end = len + off - 1;
		int cur = off;

		while (cur <= end) {
			while (!closed && !noMoreBuf && !compositeBuf.isReadable()) {
				try {
					wait();
				}
				catch (InterruptedException e) {
					throw new InternalException("wait() is wrong in " + this.getClass().getName() + ".", e);
				}
			}

			if (closed) return cur == off ? -1 : cur - off;
			else if (compositeBuf.isReadable()) {
				int fetched = Math.min(compositeBuf.readableBytes(), end - cur + 1);
				compositeBuf.readBytes(b, cur, fetched);
				cur += fetched;
			}
			else if (noMoreBuf) return cur == off ? -1 : cur - off;
		}

		return len;
	}

	/**
	 * Write {@code sourceBuf}'s content to this stream without copy.
	 * This method will take ownership of {@code sourceBuf}.
	 *
	 * @param sourceBuf A {@link ByteBuf} to write
	 */
	public synchronized void writeByteBuf(@NotNull ByteBuf sourceBuf) {
		if (closed || noMoreBuf) throw new IllegalStateException("The stream already closed");
		compositeBuf.addComponent(true, sourceBuf);
		notifyAll();
	}
}
