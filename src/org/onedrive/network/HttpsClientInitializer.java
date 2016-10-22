package org.onedrive.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import org.jetbrains.annotations.NotNull;

public class HttpsClientInitializer extends ChannelInitializer<SocketChannel> {
	private final SslContext sslCtx;
	private final ChannelHandler handler;

	public HttpsClientInitializer(@NotNull SslContext sslCtx, @NotNull ChannelHandler handler) {
		this.sslCtx = sslCtx;
		this.handler = handler;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();

		// Enable HTTPS.
		p.addLast(sslCtx.newHandler(ch.alloc()));

		p.addLast(new HttpClientCodec());

		// Remove the following line if you don't want automatic content decompression.
		p.addLast(new HttpContentDecompressor());

		// Uncomment the following line if you don't want to handle HttpContents.
		// p.addLast(new HttpObjectAggregator(1048576));

		p.addLast(handler);
	}
}
