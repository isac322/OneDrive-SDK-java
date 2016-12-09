package org.onedrive.network.async;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import org.jetbrains.annotations.NotNull;

public class AsyncDefaultInitializer extends ChannelInitializer<SocketChannel> {
	private final SslContext sslCtx;
	private final ChannelHandler handler;

	public AsyncDefaultInitializer(@NotNull SslContext sslCtx, @NotNull ChannelHandler handler) {
		this.sslCtx = sslCtx;
		this.handler = handler;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();

		// Enable HTTPS.
		p.addLast("ssl", sslCtx.newHandler(ch.alloc()));

		p.addLast("codec", new HttpClientCodec());

		// Remove the following line if you don't want automatic content decompression.
		p.addLast("inflater", new HttpContentDecompressor());

		p.addLast("handler", handler);
	}
}
