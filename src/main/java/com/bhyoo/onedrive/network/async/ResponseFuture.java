package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.utils.ByteBufStream;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface ResponseFuture extends Future<ByteBufStream> {
	HttpResponse response();

	Channel channel();

	@Override
	ResponseFuture addListener(GenericFutureListener<? extends Future<? super ByteBufStream>> listener);

	@Override
	ResponseFuture addListeners(GenericFutureListener<? extends Future<? super ByteBufStream>>[] listeners);

	@Override
	ResponseFuture removeListener(GenericFutureListener<? extends Future<? super ByteBufStream>> listener);

	@Override
	ResponseFuture removeListeners(GenericFutureListener<? extends Future<? super ByteBufStream>>[] listeners);

	@Override ResponseFuture sync() throws InterruptedException;

	@Override ResponseFuture syncUninterruptibly();

	@Override ResponseFuture await() throws InterruptedException;

	@Override ResponseFuture awaitUninterruptibly();
}
