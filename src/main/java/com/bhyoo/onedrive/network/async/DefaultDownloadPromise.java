package com.bhyoo.onedrive.network.async;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class DefaultDownloadPromise extends DefaultPromise<File> implements DownloadPromise {
	protected Path downloadPath;
	protected URL remoteUri;
	protected HttpResponse response;

	public DefaultDownloadPromise(EventExecutor executor) {
		super(executor);
	}

	@Override public Path downloadPath() {
		return downloadPath;
	}

	@Override public URL remoteURI() {
		return remoteUri;
	}

	@Override public HttpResponse response() {
		return response;
	}

	@Override public DefaultDownloadPromise setResponse(HttpResponse response) {
		this.response = response;
		return this;
	}

	@Override public DefaultDownloadPromise setURI(URL remoteUri) {
		this.remoteUri = remoteUri;
		return this;
	}

	@Override public DefaultDownloadPromise setPath(Path downloadPath) {
		this.downloadPath = downloadPath;
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
