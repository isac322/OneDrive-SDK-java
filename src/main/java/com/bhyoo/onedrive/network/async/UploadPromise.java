package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.FileItem;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface UploadPromise extends UploadFuture, Promise<FileItem> {
	@Nullable URI uploadURI();

	@NotNull UploadPromise setUploadURI(@NotNull URI uri);


	@Override UploadPromise setSuccess(FileItem result);

	@Override UploadPromise setFailure(Throwable cause);

	@Override UploadPromise addListener(GenericFutureListener<? extends Future<? super FileItem>> listener);

	@Override UploadPromise addListeners(GenericFutureListener<? extends Future<? super FileItem>>[] listeners);

	@Override UploadPromise removeListener(GenericFutureListener<? extends Future<? super FileItem>> listener);

	@Override UploadPromise removeListeners(GenericFutureListener<? extends Future<? super FileItem>>[] listeners);

	@Override UploadPromise sync() throws InterruptedException;

	@Override UploadPromise syncUninterruptibly();

	@Override UploadPromise await() throws InterruptedException;

	@Override UploadPromise awaitUninterruptibly();
}
