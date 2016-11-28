package org.onedrive.network.async;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class HttpUploadClientInitializer extends ChannelInitializer<SocketChannel> {
	private final SslContext sslCtx;
	private final ChannelHandler handler;

	public HttpUploadClientInitializer(@NotNull SslContext sslCtx, @NotNull ChannelHandler handler) {
		this.sslCtx = sslCtx;
		this.handler = handler;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));


		pipeline.addLast("codec", new HttpClientCodec());

		// Remove the following line if you don't want automatic content decompression.
		pipeline.addLast("inflater", new HttpContentDecompressor());

		// to be used since huge file transfer
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

		pipeline.addLast("handler", handler);
	}
}
