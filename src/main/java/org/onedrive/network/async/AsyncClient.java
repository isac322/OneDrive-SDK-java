package org.onedrive.network.async;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.exceptions.InternalException;

import javax.net.ssl.SSLException;
import java.net.URI;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncClient extends AbstractClient {
	@NotNull private final EventLoopGroup group;


	public AsyncClient(@NotNull EventLoopGroup group, @NotNull HttpMethod method, @NotNull URI uri) {
		super(method, uri, null);
		this.group = group;
	}

	public AsyncClient(@NotNull EventLoopGroup group, @NotNull HttpMethod method, @NotNull URI uri,
					   @Nullable byte[] content) {
		super(method, uri, content);
		this.group = group;
	}


	@Override public @NotNull AsyncClient setHeader(AsciiString header, CharSequence value) {
		request.headers().set(header, value);
		return this;
	}

	@Override public @NotNull AsyncClient setHeader(String header, String value) {
		request.headers().set(header, value);
		return this;
	}


	@Override
	public ResponseFuture execute() {
		String host = uri.getHost();
		int port = 443;

		// Configure SSL context.
		SslContext sslCtx;
		try {
			sslCtx = SslContext.newClientContext(SslProvider.JDK);
		}
		catch (SSLException e) {
			throw new InternalException("Internal SSL error while constructing. contact author.", e);
		}

		ResponsePromise promise = new DefaultResponsePromise(group.next());

		AsyncClientHandler clientHandler = new AsyncClientHandler(promise);

		// Configure the client.
		Bootstrap bootstrap = new Bootstrap()
				.group(group)
				.channel(NioSocketChannel.class)
				.handler(new AsyncDefaultInitializer(sslCtx, clientHandler));


		bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
			@Override public void operationComplete(ChannelFuture future) throws Exception {
				// Make the connection attempt.
				Channel ch = future.channel();

				// Send the HTTP request.
				ch.writeAndFlush(request);
			}
		});

		return promise;
	}
}
