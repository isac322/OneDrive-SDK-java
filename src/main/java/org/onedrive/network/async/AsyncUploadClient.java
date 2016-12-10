package org.onedrive.network.async;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;
import org.onedrive.network.RequestTool;

import static io.netty.handler.codec.http.HttpMethod.PUT;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AsyncUploadClient extends AbstractClient {
	private final UploadPromise uploadPromise;
	private final EventLoopGroup group;

	public AsyncUploadClient(@NotNull EventLoopGroup group, UploadPromise uploadPromise) {
		super(PUT, uploadPromise.uploadURI(), null);
		this.uploadPromise = uploadPromise;
		this.group = group;
	}


	@Override
	public UploadFuture execute() {
		String host = uri.getHost();
		int port = 443;

		// Configure SSL context.

		AsyncUploadHandler clientHandler = new AsyncUploadHandler(uploadPromise, request);

		// Configure the client.
		Bootstrap bootstrap = new Bootstrap()
				.group(group)
				.channel(RequestTool.socketChannelClass())
				.handler(new AsyncDefaultInitializer(clientHandler));


		bootstrap.connect(host, port);

		return uploadPromise;
	}
}
