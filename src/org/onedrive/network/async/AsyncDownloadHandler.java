package org.onedrive.network.async;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final AsynchronousFileChannel fileChannel;
	private final DownloadPromise downloadPromise;
	private long lastPosition;


	public AsyncDownloadHandler(AsynchronousFileChannel fileChannel, DownloadPromise downloadPromise) {
		this.fileChannel = fileChannel;
		this.downloadPromise = downloadPromise;
		this.lastPosition = 0;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		downloadPromise.setFailure(cause);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws IOException {
		if (msg instanceof HttpResponse) {
			downloadPromise.setResponse((HttpResponse) msg);
			// TODO: error handling, if response code isn't 200 OK
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			ByteBuf byteBuf = content.content();
			int remaining = byteBuf.readableBytes();
			fileChannel.write(byteBuf.internalNioBuffer(0, remaining), lastPosition);
			lastPosition += remaining;

			if (content instanceof LastHttpContent) {
				fileChannel.close();
				ctx.close();
				downloadPromise.trySuccess(downloadPromise.path().toFile());
			}
		}
	}
}
