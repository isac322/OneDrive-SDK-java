package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.utils.ByteBufStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface ResponsePromise extends ResponseFuture, Promise<ByteBufStream> {
	ResponsePromise setResponse(HttpResponse response);

	ResponsePromise setChannel(Channel channel);


	@Override ResponsePromise setSuccess(ByteBufStream result);

	@Override ResponsePromise setFailure(Throwable cause);

	@Override
	ResponsePromise addListener(GenericFutureListener<? extends Future<? super ByteBufStream>> listener);

	@Override
	ResponsePromise addListeners(GenericFutureListener<? extends Future<? super ByteBufStream>>[] listeners);

	@Override
	ResponsePromise removeListener(GenericFutureListener<? extends Future<? super ByteBufStream>> listener);

	@Override ResponsePromise removeListeners(
			GenericFutureListener<? extends Future<? super ByteBufStream>>[] listeners);

	@Override ResponsePromise sync() throws InterruptedException;

	@Override ResponsePromise syncUninterruptibly();

	@Override ResponsePromise await() throws InterruptedException;

	@Override ResponsePromise awaitUninterruptibly();
}
