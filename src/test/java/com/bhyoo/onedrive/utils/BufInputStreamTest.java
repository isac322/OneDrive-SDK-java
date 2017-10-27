package com.bhyoo.onedrive.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BufInputStreamTest {
	private static final byte[] first = {0, 1, 2, 3}, second = {4, 5, 6, 7};

	@Test
	public void testCase1() throws InterruptedException, IOException {

		ByteBuf buffer = Unpooled.buffer();
		final BufInputStream inputStream = new BufInputStream();

		Thread end = new Thread() {
			@Override public void run() {
				byte[] bytes = new byte[3];
				try {
					for (int i = 0; i < 4; i++) {
						int n = inputStream.read(bytes);
						System.out.println(n);
						if (n != -1) System.out.print('\t');
						for (int j = 0; j < n; j++) {
							System.out.print(bytes[j] + " ");
						}
						if (n != -1) System.out.println();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		end.start();

		TimeUnit.SECONDS.sleep(1);
		buffer.writeBytes(first);
		inputStream.setByteBuf(buffer);
		buffer.release();

		buffer = Unpooled.buffer();
		buffer.writeBytes(second);
		inputStream.setByteBuf(buffer);

		inputStream.close();

		end.join();
	}
}