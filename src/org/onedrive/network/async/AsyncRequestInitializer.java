package org.onedrive.network.async;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.jetbrains.annotations.NotNull;

public class AsyncRequestInitializer extends ChannelInitializer<SocketChannel> {
	private final SslContext sslCtx;
	private final ChannelHandler handler;

	public AsyncRequestInitializer(@NotNull SslContext sslCtx, @NotNull ChannelHandler handler) {
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

		// Uncomment the following line if you don't want to handle HttpContents.
		// p.addLast(new HttpObjectAggregator(1048576));

		// to be used since huge file transfer
		p.addLast("chunkedWriter", new ChunkedWriteHandler());

		p.addLast("handler", handler);
	}
}
