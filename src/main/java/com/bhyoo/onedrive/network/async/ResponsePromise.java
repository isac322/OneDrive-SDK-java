package com.bhyoo.onedrive.network.async;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import com.bhyoo.onedrive.utils.DirectByteInputStream;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface ResponsePromise extends ResponseFuture, Promise<DirectByteInputStream> {
	ResponsePromise setResponse(HttpResponse response);

	ResponsePromise setChannel(Channel channel);


	@Override ResponsePromise setSuccess(DirectByteInputStream result);

	@Override ResponsePromise setFailure(Throwable cause);

	@Override
	ResponsePromise addListener(GenericFutureListener<? extends Future<? super DirectByteInputStream>> listener);

	@Override
	ResponsePromise addListeners(GenericFutureListener<? extends Future<? super DirectByteInputStream>>[] listeners);

	@Override
	ResponsePromise removeListener(GenericFutureListener<? extends Future<? super DirectByteInputStream>> listener);

	@Override ResponsePromise removeListeners(
			GenericFutureListener<? extends Future<? super DirectByteInputStream>>[] listeners);

	@Override ResponsePromise sync() throws InterruptedException;

	@Override ResponsePromise syncUninterruptibly();

	@Override ResponsePromise await() throws InterruptedException;

	@Override ResponsePromise awaitUninterruptibly();
}
