package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.BaseItem;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface UploadPromise extends UploadFuture, Promise<BaseItem> {
	@Nullable URI uploadURI();

	@NotNull UploadPromise setUploadURI(@NotNull URI uri);


	@Override UploadPromise setSuccess(BaseItem result);

	@Override UploadPromise setFailure(Throwable cause);

	@Override UploadPromise addListener(GenericFutureListener<? extends Future<? super BaseItem>> listener);

	@Override UploadPromise addListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners);

	@Override UploadPromise removeListener(GenericFutureListener<? extends Future<? super BaseItem>> listener);

	@Override UploadPromise removeListeners(GenericFutureListener<? extends Future<? super BaseItem>>[] listeners);

	@Override UploadPromise sync() throws InterruptedException;

	@Override UploadPromise syncUninterruptibly();

	@Override UploadPromise await() throws InterruptedException;

	@Override UploadPromise awaitUninterruptibly();
}
