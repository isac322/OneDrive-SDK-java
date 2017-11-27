package com.bhyoo.onedrive.network.async;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface DownloadFuture extends Future<File> {
	Path downloadPath();

	URI remoteURI();

	HttpResponse response();


	@Override DownloadFuture addListener(GenericFutureListener<? extends Future<? super File>> listener);

	@Override DownloadFuture addListeners(GenericFutureListener<? extends Future<? super File>>[] listeners);

	@Override DownloadFuture removeListener(GenericFutureListener<? extends Future<? super File>> listener);

	@Override DownloadFuture removeListeners(GenericFutureListener<? extends Future<? super File>>[] listeners);

	@Override DownloadFuture sync() throws InterruptedException;

	@Override DownloadFuture syncUninterruptibly();

	@Override DownloadFuture await() throws InterruptedException;

	@Override DownloadFuture awaitUninterruptibly();
}
