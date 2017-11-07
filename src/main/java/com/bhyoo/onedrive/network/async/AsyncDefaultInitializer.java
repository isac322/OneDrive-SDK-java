package com.bhyoo.onedrive.network.async;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLException;

public class AsyncDefaultInitializer extends ChannelInitializer<SocketChannel> {
	protected static final SslContext sslContext;

	static {
		SslContext sslContext1;
		try {
			sslContext1 = SslContextBuilder.forClient().sslProvider(SslProvider.JDK).build();
		}
		catch (SSLException e) {
			e.printStackTrace();
			sslContext1 = null;
		}
		sslContext = sslContext1;
	}

	private final ChannelHandler handler;

	public AsyncDefaultInitializer(@NotNull ChannelHandler handler) {
		this.handler = handler;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();

		// Enable HTTPS.
		pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));

		pipeline.addLast("codec", new HttpClientCodec());

		// Remove the following line if you don't want automatic content decompression.
		pipeline.addLast("inflater", new HttpContentDecompressor());

		pipeline.addLast("handler", handler);
	}
}
