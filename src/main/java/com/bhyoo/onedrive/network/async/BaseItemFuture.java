package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.BaseItem;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface BaseItemFuture extends Future<BaseItem> {
	@Override BaseItemFuture addListener(GenericFutureListener<? extends Future<? super BaseItem>> listener);

	@Override BaseItemFuture addListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners);

	@Override BaseItemFuture removeListener(GenericFutureListener<? extends Future<? super BaseItem>> listener);

	@Override BaseItemFuture removeListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners);

	@Override BaseItemFuture sync() throws InterruptedException;

	@Override BaseItemFuture syncUninterruptibly();

	@Override BaseItemFuture await() throws InterruptedException;

	@Override BaseItemFuture awaitUninterruptibly();
}
