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
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final DownloadPromise promise;
	private long lastPosition;
	private FileChannel fileChannel;

	public AsyncDownloadHandler(DownloadPromise promise, @NotNull Path downloadPath) {
		this.promise = promise;
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
			byteBuf.readBytes(fileChannel, lastPosition, remaining);
			lastPosition += remaining;
			fileChannel.position(lastPosition);

			if (content instanceof LastHttpContent) {
				fileChannel.close();
				ctx.close();
				promise.trySuccess(promise.downloadPath().toFile());
			}
		}
	}
}