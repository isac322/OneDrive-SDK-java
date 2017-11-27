package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.DriveItem;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class DefaultDriveItemPromise extends DefaultPromise<DriveItem> implements DriveItemPromise {
	public DefaultDriveItemPromise(EventExecutor executor) {
		super(executor);
	}

	@Override public DefaultDriveItemPromise setSuccess(DriveItem result) {
		super.setSuccess(result);
		return this;
	}

	@Override public DefaultDriveItemPromise setFailure(Throwable cause) {
		super.setFailure(cause);
		return this;
	}

	@Override
	public DefaultDriveItemPromise addListener(GenericFutureListener<? extends Future<? super DriveItem>> listener) {
		super.addListener(listener);
		return this;
	}

	@Override
	public DefaultDriveItemPromise addListeners(
			GenericFutureListener<? extends Future<? super DriveItem>>[] listeners) {
		super.addListeners(listeners);
		return this;
	}

	@Override
	public DefaultDriveItemPromise removeListener(
			GenericFutureListener<? extends Future<? super DriveItem>> listener) {
		super.removeListener(listener);
		return this;
	}

	@Override public DefaultDriveItemPromise removeListeners(
			GenericFutureListener<? extends Future<? super DriveItem>>[] listeners) {
		super.removeListeners(listeners);
		return this;
	}

	@Override public DefaultDriveItemPromise await() throws InterruptedException {
		super.await();
		return this;
	}

	@Override public DefaultDriveItemPromise awaitUninterruptibly() {
		super.awaitUninterruptibly();
		return this;
	}

	@Override public DefaultDriveItemPromise sync() throws InterruptedException {
		super.sync();
		return this;
	}

	@Override public DefaultDriveItemPromise syncUninterruptibly() {
		super.syncUninterruptibly();
		return this;
	}
}
