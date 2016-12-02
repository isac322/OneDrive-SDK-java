package org.onedrive.network.async;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.AsciiString;
import org.jetbrains.annotations.NotNull;
import org.onedrive.exceptions.InternalException;
import org.onedrive.utils.DirectByteInputStream;
import org.onedrive.utils.RequestTool;

import javax.net.ssl.SSLException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadClient extends AbstractClient {
	@NotNull private final EventLoopGroup group;
	@NotNull private final Path downloadPath;
	@NotNull private final String accessToken;
	@NotNull private final RequestTool requestTool;

	public AsyncDownloadClient(@NotNull EventLoopGroup group, @NotNull URI uri, @NotNull Path downloadPath,
							   @NotNull String accessToken, @NotNull RequestTool requestTool) {
		super(HttpMethod.GET, uri, null);
		this.group = group;
		this.downloadPath = downloadPath;
		this.accessToken = accessToken;
		this.requestTool = requestTool;
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
		final DownloadPromise downloadPromise = new DefaultDownloadPromise(group.next(), downloadPath);

		new AsyncClient(group, method, uri, new ResponseFutureListener() {
			@Override public void operationComplete(ResponseFuture future) throws Exception {
				HttpResponse response = future.response();
				DirectByteInputStream result = future.get();

				// if response is valid
				if (future.isSuccess() && response.status().code() == HttpURLConnection.HTTP_MOVED_TEMP) {
					// handling unescaped string in Location, prepare URI.
					URL u = new URL(response.headers().get(HttpHeaderNames.LOCATION));
					URI uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(),
							u.getPath(), u.getQuery(), u.getRef());

					String host = uri.getHost();
					int port = 443;

					// set downloadPromise's URI
					downloadPromise.setURI(uri);

					// Configure SSL context.
					SslContext sslCtx;
					try {
						sslCtx = SslContextBuilder.forClient().sslProvider(SslProvider.JDK).build();
					}
					catch (SSLException e) {
						throw new InternalException("Internal SSL error while constructing. contact author.", e);
					}

					// open file channel with given path in constructor to write response data
					AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
							downloadPath,
							StandardOpenOption.CREATE,
							StandardOpenOption.WRITE,
							StandardOpenOption.TRUNCATE_EXISTING);

					AsyncDownloadHandler downloadHandler = new AsyncDownloadHandler(fileChannel, downloadPromise);

					// Configure the client.
					Bootstrap bootstrap = new Bootstrap()
							.group(group)
							.channel(NioSocketChannel.class)
							.handler(new AsyncClientInitializer(sslCtx, downloadHandler));

					// wait until be connected, and get channel
					Channel channel = bootstrap.connect(host, port).syncUninterruptibly().channel();

					// change request's url to location of file
					request.setUri(uri.toASCIIString());
					// Send the HTTP request.
					channel.writeAndFlush(request);
				}
				else if (future.isSuccess()) {
					requestTool.errorHandling(response, result, HttpURLConnection.HTTP_MOVED_TEMP);
				}
			}
		}).setHeader(HttpHeaderNames.AUTHORIZATION, accessToken).execute();

		return downloadPromise;
	}
}
