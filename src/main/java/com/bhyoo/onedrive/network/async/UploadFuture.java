package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.DriveItem;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface UploadFuture extends Future<DriveItem> {
	@NotNull Path filePath();

	@Override UploadFuture addListener(GenericFutureListener<? extends Future<? super DriveItem>> listener);

	@Override UploadFuture addListeners(GenericFutureListener<? extends Future<? super DriveItem>>[] listeners);

	@Override UploadFuture removeListener(GenericFutureListener<? extends Future<? super DriveItem>> listener);

	@Override UploadFuture removeListeners(GenericFutureListener<? extends Future<? super DriveItem>>[] listeners);

	@Override UploadFuture sync() throws InterruptedException;

	@Override UploadFuture syncUninterruptibly();

	@Override UploadFuture await() throws InterruptedException;

	@Override UploadFuture awaitUninterruptibly();
}
