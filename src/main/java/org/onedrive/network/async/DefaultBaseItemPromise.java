package org.onedrive.network.async;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.onedrive.container.items.BaseItem;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class DefaultBaseItemPromise extends DefaultPromise<BaseItem> implements BaseItemPromise {
	public DefaultBaseItemPromise(EventExecutor executor) {
		super(executor);
	}

	@Override public DefaultBaseItemPromise setSuccess(BaseItem result) {
		super.setSuccess(result);
		return this;
	}

	@Override public DefaultBaseItemPromise setFailure(Throwable cause) {
		super.setFailure(cause);
		return this;
	}

	@Override
	public DefaultBaseItemPromise addListener(GenericFutureListener<? extends Future<? super BaseItem>> listener) {
		super.addListener(listener);
		return this;
	}

	@Override
	public DefaultBaseItemPromise addListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners) {
		super.addListeners(listeners);
		return this;
	}

	@Override
	public DefaultBaseItemPromise removeListener(GenericFutureListener<? extends Future<? super BaseItem>> listener) {
		super.removeListener(listener);
		return this;
	}

	@Override public DefaultBaseItemPromise removeListeners(
			GenericFutureListener<? extends Future<? super BaseItem>>[] listeners) {
		super.removeListeners(listeners);
		return this;
	}

	@Override public DefaultBaseItemPromise await() throws InterruptedException {
		super.await();
		return this;
	}

	@Override public DefaultBaseItemPromise awaitUninterruptibly() {
		super.awaitUninterruptibly();
		return this;
	}

	@Override public DefaultBaseItemPromise sync() throws InterruptedException {
		super.sync();
		return this;
	}

	@Override public DefaultBaseItemPromise syncUninterruptibly() {
		super.syncUninterruptibly();
		return this;
	}
}
