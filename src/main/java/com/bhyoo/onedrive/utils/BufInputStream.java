package com.bhyoo.onedrive.utils;

import com.bhyoo.onedrive.exceptions.InternalException;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import static com.bhyoo.onedrive.utils.DirectByteInputStream.INDEX_EXCEPTION;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class BufInputStream extends InputStream {
	protected boolean closed;
	@Nullable protected ByteBuf byteBuf;

	@Override public synchronized void close() throws IOException {
		closed = true;
		notifyAll();
	}

	@Override
	public synchronized int read() throws IOException {
		while (!closed && byteBuf == null) {
			try {
				wait();
			}
			catch (InterruptedException e) {
				throw new InternalException("wait() is wrong in " + this.getClass().getName() + ".", e);
			}
		}

		return closed ? -1 : byteBuf.readByte();
	}

	@Override
	public synchronized int read(@NotNull byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(@NotNull byte[] b, int off, int len) throws IOException {
		if (off < 0 || len < 0 || len > b.length - off) throw INDEX_EXCEPTION;
		else if (len == 0) return 0;
		else if (closed && byteBuf == null) return -1;

		final int end = len + off - 1;
		int cur = off;

		while (cur <= end) {
			while (!closed && byteBuf == null) {
				try {
					wait();
				}
				catch (InterruptedException e) {
					throw new InternalException("wait() is wrong in " + this.getClass().getName() + ".", e);
				}
			}

			if (byteBuf != null) {
				int fetched = Math.min(byteBuf.readableBytes(), end - cur + 1);
				byteBuf.readBytes(b, cur, fetched);
				cur += fetched;

				if (byteBuf.readableBytes() == 0) {
					byteBuf.release();
					byteBuf = null;
					notifyAll();
				}
			}
			else if (closed) return cur == off ? -1 : cur - off;
		}

		return len;
	}

	public synchronized void setByteBuf(@NotNull ByteBuf byteBuf) {
		while (this.byteBuf != null) {
			try {
				wait();
			}
			catch (InterruptedException e) {
				throw new InternalException("wait() is wrong in " + this.getClass().getName() + ".", e);
			}
		}

		this.byteBuf = byteBuf;
		this.byteBuf.retain();
		notifyAll();
	}

	public synchronized void release() {
		if (byteBuf != null) byteBuf.release();
	}
}
