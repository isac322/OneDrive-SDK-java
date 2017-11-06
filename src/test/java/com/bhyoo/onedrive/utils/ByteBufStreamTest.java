package com.bhyoo.onedrive.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ByteBufStreamTest {
	private static final byte[] first = {0, 1, 2, 3}, second = {4, 5, 6, 7};

	@Test void testCase1() throws InterruptedException, IOException {
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

		TimeUnit.MILLISECONDS.sleep(50);
		buffer.writeBytes(first);
		inputStream.writeByteBuf(buffer);

		buffer = Unpooled.buffer();
		buffer.writeBytes(second);
		inputStream.writeByteBuf(buffer);

		inputStream.setNoMoreBuf();

		end.join();

		inputStream.close();
	}

	@Test void readEmptyStream() throws InterruptedException, IOException {
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

		TimeUnit.MILLISECONDS.sleep(50);

		inputStream.setNoMoreBuf();

		end.join();

		inputStream.close();
		buffer.release();
	}

	@Test void writeAfterClose() {
		final ByteBufStream stream = new ByteBufStream();
		ByteBuf buf = Unpooled.buffer(100);
		buf.writeBytes(first);

		stream.writeByteBuf(buf);
		stream.close();

		assertThrows(IllegalStateException.class, new Executable() {
			@Override public void execute() throws Throwable {
				ByteBuf buf = Unpooled.buffer(100);
				stream.writeByteBuf(buf);
			}
		});
	}

	@Test void readAfterClosed() {
		ByteBufStream stream = new ByteBufStream();
		stream.close();

		assertEquals(-1, stream.read());
	}

	@Test void readWithOffset() {
		ByteBufStream stream = new ByteBufStream();
		ByteBuf buf = Unpooled.buffer(100);
		buf.writeBytes(first);
		buf.writeBytes(second);
		stream.writeByteBuf(buf);
		stream.setNoMoreBuf();

		byte[] bytes = new byte[100];
		byte[] expectedBytes = new byte[100];

		expectedBytes[1] = first[0];
		expectedBytes[2] = first[1];
		expectedBytes[3] = first[2];
		expectedBytes[4] = first[3];
		expectedBytes[5] = second[0];

		int n = stream.read(bytes, 1, 5);
		assertEquals(5, n);

		assertArrayEquals(expectedBytes, bytes);

		stream.close();
	}
}