package org.onedrive.network.async;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.exceptions.InternalException;
import org.onedrive.utils.DirectByteInputStream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by isac322 on 16. 11. 30.
 */
public class AsyncResponseFuture {
	private final DirectByteInputStream result;
	private final Channel channel;
	private final HttpResponse[] responses;
	private final CountDownLatch responseLatch;
	private final ConcurrentHashMap<AsyncResponseHandler, CountDownLatch> allDoneMap;

	AsyncResponseFuture(DirectByteInputStream result, Channel channel, HttpResponse[] response, CountDownLatch latch) {
		this(result, channel, response, latch, null);
	}

	AsyncResponseFuture(DirectByteInputStream result, Channel channel, HttpResponse[] responses,
						CountDownLatch latch, @Nullable AsyncResponseHandler handler) {
		this.result = result;
		this.channel = channel;
		this.responses = responses;
		this.responseLatch = latch;
		this.allDoneMap = new ConcurrentHashMap<>();

		if (handler != null) this.addCompleteHandler(handler);
	}


	public void addCompleteHandler(@NotNull final AsyncResponseHandler handler) {
		final CountDownLatch latch = new CountDownLatch(1);
		allDoneMap.put(handler, latch);

		channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
			@Override public void operationComplete(Future<? super Void> future) throws Exception {
				responseLatch.await();
				handler.handle(result, responses[0]);
				latch.countDown();
			}
		});
	}

	// for legacy code. but, is it necessary??
	public HttpResponse blockingResponse() {
		try {
			responseLatch.await();
		}
		catch (InterruptedException e) {
			throw new InternalException("Error occur while waiting response latch in AsyncResponseFuture", e);
		}
		return responses[0];
	}

	public DirectByteInputStream resultStream() {
		return result;
	}

	public Channel channel() {
		return channel;
	}

	public ChannelFuture syncUntilAllDone() {
		ChannelFuture future = channel.closeFuture().syncUninterruptibly();

		for (CountDownLatch latch : allDoneMap.values()) {
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				throw new InternalException("Error occur while waiting allDone-latch in AsyncResponseFuture", e);
			}
		}

		return future;
	}
}
