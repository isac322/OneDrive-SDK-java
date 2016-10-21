package org.onedrive.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

public final class HttpsClient {
	@NotNull protected final EventLoopGroup group;
	@NotNull @Getter protected final URI uri;
	@NotNull @Getter protected final HttpMethod method;
	@NotNull protected final DefaultFullHttpRequest request;
	@Nullable @Getter @Setter protected AsyncHttpsResponseHandler onComplete;


	public HttpsClient(@NotNull EventLoopGroup group, @NotNull String url, @NotNull HttpMethod method)
			throws URISyntaxException {
		this(group, new URI(url), method, null);
	}

	public HttpsClient(@NotNull EventLoopGroup group, @NotNull URI uri, @NotNull HttpMethod method) {
		this(group, uri, method, null);
	}

	public HttpsClient(@NotNull EventLoopGroup group, @NotNull String url,
					   @NotNull HttpMethod method, @Nullable AsyncHttpsResponseHandler onComplete)
			throws URISyntaxException {
		this(group, new URI(url), method, onComplete);
	}

	public HttpsClient(@NotNull EventLoopGroup group, @NotNull URI uri,
					   @NotNull HttpMethod method, @Nullable AsyncHttpsResponseHandler onComplete) {
		this.group = group;
		this.uri = uri;
		this.method = method;
		this.request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri.getRawPath());
		this.request.headers().set(HttpHeaderNames.HOST, uri.getHost());
		this.request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);
		this.request.setUri(uri.toASCIIString());

		this.onComplete = onComplete;
	}


	public void setHeader(AsciiString headerNames, AsciiString headerValues) {
		request.headers().set(headerNames, headerValues);
	}

	public void setHeader(AsciiString header, String value) {
		request.headers().set(header, value);
	}

	@NotNull
	public HttpsClientHandler send() {
		String scheme = uri.getScheme();
		String host = uri.getHost();
		int port = 443;

		if (!"https".equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("Wrong network scheme : \"" + scheme + "\".");
		}

		// Configure SSL context.
		final SslContext sslCtx;
		try {
			sslCtx = SslContextBuilder.forClient().sslProvider(SslProvider.JDK).build();
		}
		catch (SSLException e) {
			e.printStackTrace();
			throw new RuntimeException("Internal SSL error while constructing.");
		}

		final HttpsClientHandler httpsHandler = new HttpsClientHandler(onComplete);

		// Configure the client.
		Bootstrap b = new Bootstrap()
				.group(group)
				.channel(NioSocketChannel.class)
				.handler(new HttpsClientInitializer(sslCtx, httpsHandler));


		final ChannelFuture channelFuture = b.connect(host, port);
		channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
			@Override
			public void operationComplete(Future<? super Void> future) throws Exception {
				// Make the connection attempt.
				Channel ch = channelFuture.channel();

				// Send the HTTP request.
				ch.writeAndFlush(request);

				// Wait for the server to close the connection.
				// ch.closeFuture();
			}
		});

		return httpsHandler;
	}
}
