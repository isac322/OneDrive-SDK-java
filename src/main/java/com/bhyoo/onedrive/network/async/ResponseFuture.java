package com.bhyoo.onedrive.network.async;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.bhyoo.onedrive.utils.DirectByteInputStream;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface ResponseFuture extends Future<DirectByteInputStream> {
	HttpResponse response();

	Channel channel();

	@Override
	ResponseFuture addListener(GenericFutureListener<? extends Future<? super DirectByteInputStream>> listener);

	@Override
	ResponseFuture addListeners(GenericFutureListener<? extends Future<? super DirectByteInputStream>>[] listeners);

	@Override
	ResponseFuture removeListener(GenericFutureListener<? extends Future<? super DirectByteInputStream>> listener);

	@Override
	ResponseFuture removeListeners(GenericFutureListener<? extends Future<? super DirectByteInputStream>>[] listeners);

	@Override ResponseFuture sync() throws InterruptedException;

	@Override ResponseFuture syncUninterruptibly();

	@Override ResponseFuture await() throws InterruptedException;

	@Override ResponseFuture awaitUninterruptibly();
}
