package org.onedrive.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class HttpsClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	@Nullable protected final AsyncHttpsResponseHandler onComplete;
	protected final CountDownLatch responseLatch;
	protected final CountDownLatch channelLatch;
	protected ChannelHandlerContext channelContext;
	protected HttpResponse response;
	@NotNull @Getter GrowDirectByteInputStream resultStream;


	public HttpsClientHandler(@Nullable AsyncHttpsResponseHandler onComplete) {
		this.onComplete = onComplete;
		responseLatch = new CountDownLatch(1);
		channelLatch = new CountDownLatch(1);

		resultStream = new GrowDirectByteInputStream();
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		channelContext = ctx;
		channelLatch.countDown();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
		if (msg instanceof HttpResponse) {
			this.response = (HttpResponse) msg;
			responseLatch.countDown();

/*
			System.err.println("STATUS: " + response.status());
			System.err.println("VERSION: " + response.protocolVersion());
			System.err.println();

			if (!response.headers().isEmpty()) {
				for (CharSequence name : response.headers().names()) {
					for (CharSequence value : response.headers().getAll(name)) {
						System.err.println("HEADER: " + name + " = " + value);
					}
				}
				System.err.println();
			}

			if (HttpUtil.isTransferEncodingChunked(response)) {
				System.err.println("CHUNKED CONTENT {");
			}
			else {
				System.err.println("CONTENT {");
			}
*/
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
//			System.err.println("receiving");

			ByteBuf byteBuf = content.content();
			int remaining = byteBuf.readableBytes();
			resultStream.ensureRemain(remaining);
			byteBuf.readBytes(resultStream.getRawBuffer(), resultStream.in + 1, remaining);
			resultStream.jumpTo(resultStream.in + remaining);

			if (content instanceof LastHttpContent) {
//				System.err.println("} END OF CONTENT");

				resultStream.close();

				if (onComplete != null) {
					onComplete.handle(resultStream, response);
				}

				ctx.close();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}


	public void addCloseListener(@NotNull final AsyncHttpsResponseHandler beforeCloseHandler) {
		getBlockingCloseFuture().addListener(
				new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						assert response != null;
						beforeCloseHandler.handle(resultStream, response);
					}
				});
	}


	@SneakyThrows(InterruptedException.class)
	public HttpResponse getBlockingResponse() {
		responseLatch.await();
		return response;
	}

	@SneakyThrows(InterruptedException.class)
	public ChannelHandlerContext getBlockingChannelContext() {
		channelLatch.await();
		return channelContext;
	}

	public ChannelFuture getBlockingCloseFuture() {
		ChannelHandlerContext channelContext = getBlockingChannelContext();
		return channelContext.channel().closeFuture();
	}
}
