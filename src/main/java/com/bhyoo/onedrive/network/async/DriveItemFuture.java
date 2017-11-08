package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.DriveItem;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface DriveItemFuture extends Future<DriveItem> {
	@Override DriveItemFuture addListener(GenericFutureListener<? extends Future<? super DriveItem>> listener);

	@Override DriveItemFuture addListeners(GenericFutureListener<? extends Future<? super DriveItem>>[] listeners);

	@Override DriveItemFuture removeListener(GenericFutureListener<? extends Future<? super DriveItem>> listener);

	@Override DriveItemFuture removeListeners(GenericFutureListener<? extends Future<? super DriveItem>>[] listeners);

	@Override DriveItemFuture sync() throws InterruptedException;

	@Override DriveItemFuture syncUninterruptibly();

	@Override DriveItemFuture await() throws InterruptedException;

	@Override DriveItemFuture awaitUninterruptibly();
}
