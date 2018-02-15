package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.utils.ByteBufStream;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AsciiString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AsyncDownloadClient extends AbstractClient {
	private final @NotNull String accessToken;
	private final @NotNull Path downloadFolder;
	private final @NotNull RequestTool requestTool;
	private final @Nullable String newName;


	public AsyncDownloadClient(@NotNull RequestTool requestTool, @NotNull URI itemURI, @NotNull Path downloadFolder) {
		this(requestTool, itemURI, downloadFolder, null);
	}

	public AsyncDownloadClient(@NotNull RequestTool requestTool, @NotNull URI itemURI,
							   @NotNull Path downloadFolder, @Nullable String newName) {
		super(HttpMethod.GET, itemURI, null);
		this.requestTool = requestTool;
		this.accessToken = requestTool.getClient().getFullToken();
		this.downloadFolder = downloadFolder;
		this.newName = newName;
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
		EventLoopGroup group = RequestTool.group();

		DownloadPromise downloadPromise = new DefaultDownloadPromise(group.next())
				.setPath(downloadFolder);

		DownloadListener listener = new DownloadListener(downloadPromise, request, requestTool, newName);

		new AsyncClient(group, method, uri)
				.setHeader(HttpHeaderNames.AUTHORIZATION, accessToken)
				.execute()
				.addListener(listener);

		return downloadPromise;
	}

	static class DownloadListener implements ResponseFutureListener {
		private final DownloadPromise promise;
		private final DefaultFullHttpRequest request;
		private final RequestTool requestTool;
		private final @Nullable String newName;

		DownloadListener(DownloadPromise promise, DefaultFullHttpRequest request,
						 RequestTool requestTool, @Nullable String newName) {
			this.promise = promise;
			this.request = request;
			this.requestTool = requestTool;
			this.newName = newName;
		}

		@Override public void operationComplete(ResponseFuture future)
				throws ExecutionException, InterruptedException, URISyntaxException, ErrorResponseException {
			HttpResponse response = future.response();
			ByteBufStream result = future.get();

			// if response is valid
			if (!future.isSuccess()) {
				// TODO: handle error
			}
			else if (response.status().code() == HTTP_MOVED_TEMP) {
				String uriStr = response.headers().get(HttpHeaderNames.LOCATION);
				URI uri = new URI(uriStr);

				String host = uri.getHost();
				int port = 443;

				// set downloadPromise's URI
				promise.setURI(uri);

				// change request's url to location of file
				request.setUri(uriStr);

				AsyncDownloadHandler downloadHandler = new AsyncDownloadHandler(promise, newName);

				// Configure the client.
				Bootstrap bootstrap = new Bootstrap()
						.group(RequestTool.group())
						.channel(RequestTool.socketChannelClass())
						.handler(new AsyncDefaultInitializer(downloadHandler));

				// wait until be connected, and get channel
				Channel channel = bootstrap.connect(host, port).syncUninterruptibly().channel();

				// Send the HTTP request.
				channel.writeAndFlush(request);
			}
			else {
				try {
					requestTool.errorHandling(response, result, HTTP_MOVED_TEMP);
				}
				catch (Exception e) {
					promise.setFailure(e);
					throw e;
				}
			}
		}
	}
}
