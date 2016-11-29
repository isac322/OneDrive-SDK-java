package org.onedrive.network.async;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.onedrive.utils.DirectByteInputStream;

import java.util.concurrent.CountDownLatch;

public class AsyncRequestHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final DirectByteInputStream resultStream;
	private final HttpResponse[] responses;
	private final CountDownLatch responseLatch;


	public AsyncRequestHandler(DirectByteInputStream resultStream, HttpResponse[] responses, CountDownLatch latch) {
		this.resultStream = resultStream;
		this.responses = responses;
		this.responseLatch = latch;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
		if (msg instanceof HttpResponse) {
			responses[0] = (HttpResponse) msg;
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
			byteBuf.readBytes(resultStream.rawBuffer(), resultStream.getIn() + 1, remaining);
			resultStream.jumpTo(resultStream.getIn() + remaining);

			if (content instanceof LastHttpContent) {
//				System.err.println("} END OF CONTENT");

				resultStream.close();

				ctx.close();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
