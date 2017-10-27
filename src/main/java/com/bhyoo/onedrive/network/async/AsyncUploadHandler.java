package com.bhyoo.onedrive.network.async;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static java.net.HttpURLConnection.*;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AsyncUploadHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final int UPLOAD_FRAGMENT_SIZE_MIN = 320 * 1024;
	private static final int UPLOAD_FRAGMENT_SIZE_MAX = 320 * 1024 * 32;
	private final UploadPromise promise;
	private final FullHttpRequest request;
	private final ByteBuf byteBuf;
	private FileChannel fileChannel;
	private int status;
	private int currentFragSize;
	private long currentFilePosition;

	public AsyncUploadHandler(UploadPromise promise, DefaultFullHttpRequest request) {
		this.promise = promise;
		this.byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(UPLOAD_FRAGMENT_SIZE_MAX);
		// TODO: `AsyncUploadClient` would be useless (make request independently in refactoring)
		request.content().release();
		this.request = request.replace(byteBuf);
		this.request.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);

		currentFragSize = 0;
		currentFilePosition = 0;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		fileChannel = new FileInputStream(promise.filePath().toFile()).getChannel();
		ctx.writeAndFlush(nextRequest());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		promise.setFailure(cause);
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		ctx.flush();
	}

	private FullHttpRequest nextRequest() throws IOException {
		if (currentFragSize < UPLOAD_FRAGMENT_SIZE_MAX) currentFragSize += UPLOAD_FRAGMENT_SIZE_MIN << 2;

		// reset internal buffer status
		byteBuf.clear().writerIndex(currentFragSize).retain();

		// read from file channel
		ByteBuffer nioBuffer = byteBuf.internalNioBuffer(0, currentFragSize);
		int readBytes = fileChannel.read(nioBuffer);

		long oldPosition = currentFilePosition;
		currentFilePosition += readBytes;

		// update request with new range
		request.headers()
				.set(CONTENT_LENGTH, String.valueOf(readBytes))
				.set(CONTENT_RANGE,
						"bytes " + oldPosition + '-' + (currentFilePosition - 1) + '/' + fileChannel.size());

		// if this is last request, release internal buffer
		if (currentFilePosition == fileChannel.size()) {
			request.headers().set(CONNECTION, CLOSE);
			byteBuf.release();
		}

		return request;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			status = response.status().code();
		}

		if (msg instanceof HttpContent) {
			if (msg instanceof LastHttpContent) {
				switch (status) {
					case HTTP_ACCEPTED:
						ctx.write(nextRequest());
						break;
					case HTTP_OK:
					case HTTP_CREATED:
						ctx.close();
						fileChannel.close();
						// TODO: fill argument
						promise.trySuccess(null);
						break;
					default:
						break;
				}
			}
		}
	}
}
