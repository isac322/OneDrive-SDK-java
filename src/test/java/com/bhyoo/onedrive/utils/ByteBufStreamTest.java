package com.bhyoo.onedrive.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ByteBufStreamTest {
	private static final byte[] first = {0, 1, 2, 3}, second = {4, 5, 6, 7};

	@Test
	public void testCase1() throws InterruptedException, IOException {
		ByteBuf buffer = Unpooled.buffer();
		final ByteBufStream inputStream = new ByteBufStream();

		Thread end = new Thread() {
			@Override public void run() {
				byte[] bytes = new byte[3];
				int n;

				n = inputStream.read(bytes);
				assertEquals(3, n);
				assertEquals(first[0], bytes[0]);
				assertEquals(first[1], bytes[1]);
				assertEquals(first[2], bytes[2]);

				n = inputStream.read(bytes);
				assertEquals(3, n);
				assertEquals(first[3], bytes[0]);
				assertEquals(second[0], bytes[1]);
				assertEquals(second[1], bytes[2]);

				n = inputStream.read(bytes);
				assertEquals(2, n);
				assertEquals(second[2], bytes[0]);
				assertEquals(second[3], bytes[1]);

				n = inputStream.read(bytes);
				assertEquals(-1, n);
			}
		};
		end.start();

		TimeUnit.SECONDS.sleep(1);
		buffer.writeBytes(first);
		inputStream.writeByteBuf(buffer);

		buffer = Unpooled.buffer();
		buffer.writeBytes(second);
		inputStream.writeByteBuf(buffer);

		inputStream.setNoMoreBuf();

		end.join();

		inputStream.close();
	}

	@Test
	public void emptyStreamRead() throws InterruptedException, IOException {
		ByteBuf buffer = Unpooled.buffer();
		final ByteBufStream inputStream = new ByteBufStream();

		Thread end = new Thread() {
			@Override public void run() {
				byte[] bytes = new byte[3];
				int n;

				n = inputStream.read(bytes);
				assertEquals(-1, n);

				assertEquals(-1, inputStream.read());
			}
		};
		end.start();

		TimeUnit.SECONDS.sleep(1);

		inputStream.setNoMoreBuf();

		end.join();

		inputStream.close();
		buffer.release();
	}

	@Test
	public void writeAfterClose() {
		ByteBufStream stream = new ByteBufStream();
		ByteBuf buf = Unpooled.buffer(100);
		buf.writeBytes(first);

		stream.writeByteBuf(buf);
		stream.close();

		buf = Unpooled.buffer(100);
		stream.writeByteBuf(buf);
	}
}