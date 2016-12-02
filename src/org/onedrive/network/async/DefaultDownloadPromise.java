package org.onedrive.network.async;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class DefaultDownloadPromise extends DefaultPromise<File> implements DownloadPromise {
	protected final Path path;
	protected URI uri;
	protected HttpResponse response;

	public DefaultDownloadPromise(EventExecutor executor, Path path) {
		super(executor);
		this.path = path;
	}

	@Override public Path path() {
		return path;
	}

	@Override public URI uri() {
		return uri;
	}

	@Override public HttpResponse response() {
		return response;
	}

	@Override public DownloadPromise setResponse(HttpResponse response) {
		this.response = response;
		return this;
	}

	@Override public DownloadPromise setURI(URI uri) {
		this.uri = uri;
		return this;
	}


	@Override public DefaultDownloadPromise setSuccess(File result) {
		super.setSuccess(result);
		return this;
	}

	@Override public DefaultDownloadPromise setFailure(Throwable cause) {
		super.setFailure(cause);
		return this;
	}

	@Override
	public DefaultDownloadPromise addListener(GenericFutureListener<? extends Future<? super File>> listener) {
		super.addListener(listener);
		return this;
	}

	@Override
	public DefaultDownloadPromise addListeners(GenericFutureListener<? extends Future<? super File>>[] listeners) {
		super.addListeners(listeners);
		return this;
	}

	@Override
	public DefaultDownloadPromise removeListener(GenericFutureListener<? extends Future<? super File>> listener) {
		super.removeListener(listener);
		return this;
	}

	@Override
	public DefaultDownloadPromise removeListeners(GenericFutureListener<? extends Future<? super File>>[] listeners) {
		super.removeListeners(listeners);
		return this;
	}

	@Override public DefaultDownloadPromise await() throws InterruptedException {
		super.await();
		return this;
	}

	@Override public DefaultDownloadPromise awaitUninterruptibly() {
		super.awaitUninterruptibly();
		return this;
	}

	@Override public DefaultDownloadPromise sync() throws InterruptedException {
		super.sync();
		return this;
	}

	@Override public DefaultDownloadPromise syncUninterruptibly() {
		super.syncUninterruptibly();
		return this;
	}
}
