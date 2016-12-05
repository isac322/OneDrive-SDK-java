package org.onedrive.network.async;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadHandler extends SimpleChannelInboundHandler<HttpObject> {

	private final DownloadPromise promise;
	private Path downloadPath;
	private long lastPosition;
	private AsynchronousFileChannel fileChannel;

	public AsyncDownloadHandler(DownloadPromise promise, @NotNull Path downloadPath) {
		this.promise = promise;
		this.downloadPath = downloadPath;
		this.lastPosition = 0;

		promise.setPath(downloadPath);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		promise.setFailure(cause);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws IOException {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			promise.setResponse(response);
			// TODO: error handling, if response code isn't 200 OK

			/*
			if (newName == null) {
				String disposition = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
				System.out.println(disposition);
				newName = Extractor.extract(disposition);
				System.out.println(newName);

				// example : %5b%25EC%2588%2598%25EC%25A0%2595%5d%2520vol.5.mp3 is actually [수정] vol.5.mp3 in korean
				// but if you decode only once, result is [%EC%88%98%EC%A0%95]%20vol.5.mp3
				// and if you decode one more time from here finally becomes valid file name.
				newName = QueryStringDecoder.decodeComponent(newName);
				newName = QueryStringDecoder.decodeComponent(newName);
				System.out.println(newName);
			}

			promise.setPath(downloadPath.resolve(newName));
			*/

			// open file channel with given path in promise to write response data
			this.fileChannel = AsynchronousFileChannel.open(
					promise.downloadPath(),
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);

			promise.setChannel(fileChannel);
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			ByteBuf byteBuf = content.content();
			int remaining = byteBuf.readableBytes();
			fileChannel.write(byteBuf.nioBuffer(0, remaining), lastPosition);
			lastPosition += remaining;

			if (content instanceof LastHttpContent) {
				fileChannel.close();
				ctx.close();
				promise.trySuccess(promise.downloadPath().toFile());
			}
		}
	}

	// i don't know why i made this by myself with this... big and nasty codes ;)
	static class Extractor {
		private static final int STATE_NONE = 0;
		private static final int STATE_F = 1;
		private static final int STATE_FI = 2;
		private static final int STATE_FIL = 3;
		private static final int STATE_FILE = 4;
		private static final int STATE_FILEN = 5;
		private static final int STATE_FILENA = 6;
		private static final int STATE_FILENAM = 7;
		private static final int STATE_FILENAME = 8;
		private static final int STATE_UTF_ONGOING = 9;
		private static final int STATE_UTF_FINISH = 10;
		private static final int STATE_UTF_STAR = 11;
		private static final int STATE_UTF_EQUAL = 12;
		private static final int STATE_UTF_U = 13;
		private static final int STATE_UTF_UT = 14;
		private static final int STATE_UTF_UTF = 15;
		private static final int STATE_UTF_UTF_ = 16;
		private static final int STATE_UTF_UTF_8 = 17;
		private static final int STATE_UTF_UTF_8_1 = 18;
		private static final int STATE_ASCII_EQUAL = 19;
		private static final int STATE_ASCII_ONGOING = 20;
		private static final int STATE_ASCII_FINISH = 21;

		static String extract(String contentDisposition) {
			int begin = -1, end = -1;
			int state = STATE_NONE;

			for (int i = 0, len = contentDisposition.length(); i < len; i++) {
				char ch = contentDisposition.charAt(i);

				switch (state) {
					case STATE_NONE:
						state = ch == 'f' || ch == 'F' ? STATE_F : STATE_NONE;
						break;
					case STATE_F:
						state = ch == 'i' || ch == 'I' ? STATE_FI : STATE_NONE;
						break;
					case STATE_FI:
						state = ch == 'l' || ch == 'L' ? STATE_FIL : STATE_NONE;
						break;
					case STATE_FIL:
						state = ch == 'e' || ch == 'E' ? STATE_FILE : STATE_NONE;
						break;
					case STATE_FILE:
						state = ch == 'n' || ch == 'N' ? STATE_FILEN : STATE_NONE;
						break;
					case STATE_FILEN:
						state = ch == 'a' || ch == 'A' ? STATE_FILENA : STATE_NONE;
						break;
					case STATE_FILENA:
						state = ch == 'm' || ch == 'M' ? STATE_FILENAM : STATE_NONE;
						break;
					case STATE_FILENAM:
						state = ch == 'e' || ch == 'E' ? STATE_FILENAME : STATE_NONE;
						break;
					case STATE_FILENAME:
						if (ch == '*') state = STATE_UTF_STAR;
						else if (ch == '=') state = STATE_ASCII_EQUAL;
						else state = STATE_NONE;
						break;
					case STATE_ASCII_EQUAL:
						if (ch == '\"') {
							state = STATE_ASCII_ONGOING;
							begin = i + 1;
						}
						else state = STATE_NONE;
						break;
					case STATE_ASCII_ONGOING:
						if (ch == '\"') {
							state = STATE_ASCII_FINISH;
							end = i;
							i = len;
						}
						break;
					case STATE_UTF_STAR:
						state = ch == '=' ? STATE_UTF_EQUAL : STATE_NONE;
						break;
					case STATE_UTF_EQUAL:
						state = ch == 'U' || ch == 'u' ? STATE_UTF_U : STATE_NONE;
						break;
					case STATE_UTF_U:
						state = ch == 'T' || ch == 't' ? STATE_UTF_UT : STATE_NONE;
						break;
					case STATE_UTF_UT:
						state = ch == 'F' || ch == 'f' ? STATE_UTF_UTF : STATE_NONE;
						break;
					case STATE_UTF_UTF:
						state = ch == '-' ? STATE_UTF_UTF_ : STATE_NONE;
						break;
					case STATE_UTF_UTF_:
						state = ch == '8' ? STATE_UTF_UTF_8 : STATE_NONE;
						break;
					case STATE_UTF_UTF_8:
						state = ch == '\'' ? STATE_UTF_UTF_8_1 : STATE_NONE;
						break;
					case STATE_UTF_UTF_8_1:
						if (ch == '\'') {
							state = STATE_UTF_ONGOING;
							begin = i + 1;
						}
						else state = STATE_NONE;
						break;
					case STATE_UTF_ONGOING:
						if (ch == ';') {
							state = STATE_UTF_FINISH;
							end = i;
							i = len;
						}
						break;
				}
			}

			String ret;
			switch (state) {
				case STATE_ASCII_FINISH:
				case STATE_UTF_FINISH:
					ret = contentDisposition.substring(begin, end);
					break;
				case STATE_UTF_ONGOING:
					ret = contentDisposition.substring(begin);
					break;
				default:
					assert true : "Wrong state in AsyncDownloadHandler.Extractor";
					throw new IllegalStateException("Wrong state in AsyncDownloadHandler.Extractor");
			}

			return ret;
		}
	}
}