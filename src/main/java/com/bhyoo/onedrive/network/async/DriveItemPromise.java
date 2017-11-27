package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.DriveItem;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface DriveItemPromise extends DriveItemFuture, Promise<DriveItem> {
	@Override DriveItemPromise setSuccess(DriveItem result);

	@Override DriveItemPromise setFailure(Throwable cause);

	@Override DriveItemPromise addListener(GenericFutureListener<? extends Future<? super DriveItem>> listener);

	@Override DriveItemPromise addListeners(GenericFutureListener<? extends Future<? super DriveItem>>[] listeners);

	@Override DriveItemPromise removeListener(GenericFutureListener<? extends Future<? super DriveItem>> listener);

	@Override DriveItemPromise removeListeners(GenericFutureListener<? extends Future<? super DriveItem>>[] listeners);

	@Override DriveItemPromise sync() throws InterruptedException;

	@Override DriveItemPromise syncUninterruptibly();

	@Override DriveItemPromise await() throws InterruptedException;

	@Override DriveItemPromise awaitUninterruptibly();
}
