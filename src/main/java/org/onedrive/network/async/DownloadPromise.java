package org.onedrive.network.async;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public interface DownloadPromise extends DownloadFuture, Promise<File> {
	DownloadPromise setResponse(HttpResponse response);

	DownloadPromise setURI(URI remoteUri);

	DownloadPromise setPath(Path downloadPath);


	@Override DownloadPromise setSuccess(File result);

	@Override DownloadPromise setFailure(Throwable cause);

	@Override DownloadPromise addListener(GenericFutureListener<? extends Future<? super File>> listener);

	@Override DownloadPromise addListeners(GenericFutureListener<? extends Future<? super File>>[] listeners);

	@Override DownloadPromise removeListener(GenericFutureListener<? extends Future<? super File>> listener);

	@Override DownloadPromise removeListeners(GenericFutureListener<? extends Future<? super File>>[] listeners);

	@Override DownloadPromise sync() throws InterruptedException;

	@Override DownloadPromise syncUninterruptibly();

	@Override DownloadPromise await() throws InterruptedException;

	@Override DownloadPromise awaitUninterruptibly();
}
