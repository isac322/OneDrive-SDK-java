package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.utils.ByteBufStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

public class AsyncClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final ByteBufStream resultStream = new ByteBufStream();
	private final ResponsePromise responsePromise;


	public AsyncClientHandler(ResponsePromise responsePromise) {
		this.responsePromise = responsePromise;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		responsePromise.setChannel(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		responsePromise.setFailure(cause);
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			responsePromise.setResponse((HttpResponse) msg);
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			resultStream.writeByteBuf(content.content());

			// if this message is last of response
			if (content instanceof LastHttpContent) {
				ctx.close();
				responsePromise.trySuccess(resultStream);
			}
		}
	}
}
