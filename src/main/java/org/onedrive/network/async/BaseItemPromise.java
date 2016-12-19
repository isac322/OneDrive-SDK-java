package org.onedrive.network.async;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.onedrive.container.items.BaseItem;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public interface BaseItemPromise extends BaseItemFuture, Promise<BaseItem> {
	@Override BaseItemPromise setSuccess(BaseItem result);

	@Override BaseItemPromise setFailure(Throwable cause);

	@Override BaseItemPromise addListener(GenericFutureListener<? extends Future<? super BaseItem>> listener);

	@Override BaseItemPromise addListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners);

	@Override BaseItemPromise removeListener(GenericFutureListener<? extends Future<? super BaseItem>> listener);

	@Override BaseItemPromise removeListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners);

	@Override BaseItemPromise sync() throws InterruptedException;

	@Override BaseItemPromise syncUninterruptibly();

	@Override BaseItemPromise await() throws InterruptedException;

	@Override BaseItemPromise awaitUninterruptibly();
}
