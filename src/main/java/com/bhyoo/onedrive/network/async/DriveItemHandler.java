package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.container.items.DriveItem;
import com.bhyoo.onedrive.utils.ByteBufStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class DriveItemHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final @NotNull DefaultDriveItemPromise promise;
	private final @NotNull RequestTool requestTool;
	private final ByteBufStream stream;
	private HttpResponse response;
	private DriveItem driveItem;
	private Thread workerThread;
	private @Nullable Exception workerException;

	public DriveItemHandler(@NotNull DefaultDriveItemPromise promise, @NotNull RequestTool requestTool) {
		this.promise = promise;
		this.requestTool = requestTool;
		this.stream = new ByteBufStream();
	}

	@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		promise.setFailure(cause);
		if (!stream.isClosed()) stream.close();
		ctx.close();
	}

	@Override protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			this.response = (HttpResponse) msg;

			workerThread = new Thread() {
				@Override public void run() {
					try {
						driveItem = requestTool.parseDriveItemAndHandle(response, stream, HTTP_OK);
					}
					catch (Exception e) {
						workerException = e;
					}
				}
			};
			workerThread.start();
		}
		else if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			if (content instanceof LastHttpContent) {
				ctx.close();
				stream.setNoMoreBuf();
				workerThread.join();

				if (workerException != null) {
					promise.setFailure(workerException);
				}
				else if (response.status().code() == HTTP_OK) {
					promise.setSuccess(driveItem);
				}
				else {
					throw new IllegalStateException(
							"HTTP response code is not " + HTTP_OK + " and not occurs any exception");
				}
			}
			else {
				stream.writeByteBuf(content.content());
			}
		}
	}
}
