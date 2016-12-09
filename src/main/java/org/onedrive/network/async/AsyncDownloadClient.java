package org.onedrive.network.async;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import org.jetbrains.annotations.NotNull;
import org.onedrive.exceptions.InternalException;
import org.onedrive.network.RequestTool;
import org.onedrive.utils.DirectByteInputStream;

import javax.net.ssl.SSLException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadClient extends AbstractClient {
	@NotNull private final EventLoopGroup group;
	@NotNull private final String accessToken;
	@NotNull private final DownloadPromise downloadPromise;
	@NotNull private final ResponseFutureListener listener;


	public AsyncDownloadClient(@NotNull RequestTool requestTool, @NotNull URI itemURI, @NotNull Path downloadFolder) {
		super(HttpMethod.GET, itemURI, null);
		this.group = RequestTool.getGroup();
		this.accessToken = requestTool.getClient().getFullToken();

		downloadPromise = new DefaultDownloadPromise(group.next());
		this.listener = new WithoutNameListener(downloadPromise, downloadFolder, request, requestTool);
	}

	public AsyncDownloadClient(@NotNull RequestTool requestTool, @NotNull URI uri,
							   @NotNull Path downloadFolder, @NotNull String newName) {
		super(HttpMethod.GET, uri, null);
		this.group = RequestTool.getGroup();
		this.accessToken = requestTool.getClient().getFullToken();
		downloadFolder = downloadFolder.resolve(newName);

		downloadPromise = new DefaultDownloadPromise(group.next());
		this.listener = new WithNameListener(downloadPromise, downloadFolder, request, requestTool);
	}

	@Override public @NotNull AsyncDownloadClient setHeader(AsciiString header, CharSequence value) {
		super.setHeader(header, value);
		return this;
	}

	@Override public @NotNull AsyncDownloadClient setHeader(String header, String value) {
		super.setHeader(header, value);
		return this;
	}

	@Override
	public DownloadFuture execute() {
		new AsyncClient(group, method, uri)
				.setHeader(HttpHeaderNames.AUTHORIZATION, accessToken)
				.execute()
				.addListener(listener);

		return downloadPromise;
	}

	static class WithNameListener implements ResponseFutureListener {
		private final EventLoopGroup group;
		private final DownloadPromise promise;
		private final Path downloadPath;
		private final DefaultFullHttpRequest request;
		private final RequestTool requestTool;

		WithNameListener(DownloadPromise promise, Path downloadPath,
						 DefaultFullHttpRequest request, RequestTool requestTool) {
			this.promise = promise;
			this.downloadPath = downloadPath;
			this.request = request;
			this.requestTool = requestTool;
			this.group = RequestTool.getGroup();
		}

		@Override public void operationComplete(ResponseFuture future) throws Exception {
			HttpResponse response = future.response();
			DirectByteInputStream result = future.get();

			// if response is valid
			if (future.isSuccess() && response.status().code() == HttpURLConnection.HTTP_MOVED_TEMP) {
				// handling unescaped string in Location, prepare URI.
				URL u = new URL(response.headers().get(HttpHeaderNames.LOCATION).toString());
				URI uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(),
						u.getPath(), u.getQuery(), u.getRef());

				String host = uri.getHost();
				int port = 443;

				// set downloadPromise's URI
				promise.setURI(uri);

				// Configure SSL context.
				SslContext sslCtx;
				try {
					sslCtx = SslContext.newClientContext(SslProvider.JDK);
				}
				catch (SSLException e) {
					throw new InternalException("Internal SSL error while constructing. contact author.", e);
				}

				AsyncDownloadHandler downloadHandler = new AsyncDownloadHandler(promise, downloadPath);

				// Configure the client.
				Bootstrap bootstrap = new Bootstrap()
						.group(group)
						.channel(NioSocketChannel.class)
						.handler(new AsyncDefaultInitializer(sslCtx, downloadHandler));

				// wait until be connected, and get channel
				Channel channel = bootstrap.connect(host, port).syncUninterruptibly().channel();

				// change request's url to location of file
				request.setUri(uri.toASCIIString());
				// Send the HTTP request.
				channel.writeAndFlush(request);
			}
			else if (future.isSuccess()) {
				try {
					requestTool.errorHandling(response, result, HttpURLConnection.HTTP_MOVED_TEMP);
				}
				catch (Exception e) {
					promise.setFailure(e);
					throw e;
				}

			}
		}
	}

	static class WithoutNameListener implements ResponseFutureListener {
		private final EventLoopGroup group;
		private final DownloadPromise promise;
		private Path downloadPath;
		private final DefaultFullHttpRequest request;
		private final RequestTool requestTool;

		WithoutNameListener(DownloadPromise promise, Path downloadPath,
							DefaultFullHttpRequest request, RequestTool requestTool) {
			this.promise = promise;
			this.downloadPath = downloadPath;
			this.request = request;
			this.requestTool = requestTool;
			this.group = RequestTool.getGroup();
		}

		@Override public void operationComplete(ResponseFuture future) throws Exception {
			HttpResponse response = future.response();
			DirectByteInputStream result = future.get();

			// if response is valid
			if (future.isSuccess() && response.status().code() == HttpURLConnection.HTTP_OK) {
				// FIXME: speed up
				JsonNode jsonNode = requestTool.getClient().mapper().readTree(result);
				URI uri = new URI(jsonNode.get("@content.downloadUrl").asText());
				downloadPath = downloadPath.resolve(jsonNode.get("name").asText());

				String host = uri.getHost();
				int port = 443;

				// set downloadPromise's URI
				promise.setURI(uri);

				// Configure SSL context.
				SslContext sslCtx;
				try {
					sslCtx = SslContext.newClientContext(SslProvider.JDK);
				}
				catch (SSLException e) {
					throw new InternalException("Internal SSL error while constructing. contact author.", e);
				}

				AsyncDownloadHandler downloadHandler = new AsyncDownloadHandler(promise, downloadPath);

				// Configure the client.
				Bootstrap bootstrap = new Bootstrap()
						.group(group)
						.channel(NioSocketChannel.class)
						.handler(new AsyncDefaultInitializer(sslCtx, downloadHandler));

				// wait until be connected, and get channel
				Channel channel = bootstrap.connect(host, port).syncUninterruptibly().channel();

				// change request's url to location of file
				request.setUri(uri.toASCIIString());
				// Send the HTTP request.
				channel.writeAndFlush(request);
			}
			else if (future.isSuccess()) {
				try {
					requestTool.errorHandling(response, result, HttpURLConnection.HTTP_OK);
				}
				catch (Exception e) {
					promise.setFailure(e);
					throw e;
				}

			}
		}
	}
}
