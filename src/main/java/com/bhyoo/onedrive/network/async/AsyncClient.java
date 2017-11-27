package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.client.RequestTool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AsciiString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
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

		ResponsePromise promise = new DefaultResponsePromise(group.next());

		// Configure the client.
		Bootstrap bootstrap = new Bootstrap()
				.group(group)
				.channel(RequestTool.socketChannelClass())
				.handler(new AsyncDefaultInitializer(new AsyncClientHandler(promise)));


		bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
			@Override public void operationComplete(ChannelFuture future) throws Exception {
				future.channel().writeAndFlush(request);
			}
		});

		return promise;
	}
}
