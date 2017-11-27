package com.bhyoo.onedrive.network.async;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final DownloadPromise promise;
	private FileChannel fileChannel;

	public AsyncDownloadHandler(DownloadPromise promise, @NotNull Path downloadPath) {
		this.promise = promise;

		promise.setPath(downloadPath);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		promise.setFailure(cause);
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			promise.setResponse(response);
			// TODO: error handling, if response code isn't 200 OK

			// open file channel with given path in promise to write response data
			this.fileChannel = FileChannel.open(
					promise.downloadPath(),
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			ByteBuf byteBuf = content.content();
			int remaining = byteBuf.readableBytes();

			ByteBuffer nioBuffer = byteBuf.internalNioBuffer(0, remaining);
			fileChannel.write(nioBuffer);

			if (content instanceof LastHttpContent) {
				fileChannel.close();
				ctx.close();
				promise.trySuccess(promise.downloadPath().toFile());
			}
		}
	}
}