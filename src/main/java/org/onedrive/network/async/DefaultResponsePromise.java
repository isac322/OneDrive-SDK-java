package org.onedrive.network.async;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.onedrive.utils.DirectByteInputStream;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class DefaultResponsePromise extends DefaultPromise<DirectByteInputStream> implements ResponsePromise {
	protected HttpResponse response;
	protected Channel channel;

	public DefaultResponsePromise(EventExecutor executor) {
		super(executor);
	}

	@Override public HttpResponse response() {
		return response;
	}

	@Override public ResponsePromise setResponse(HttpResponse response) {
		this.response = response;
		return this;
	}

	@Override public ResponsePromise setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	@Override public Channel channel() {
		return channel;
	}

	@Override public boolean trySuccess(DirectByteInputStream result) {
		result.close();
		return super.trySuccess(result);
	}

	@Override public ResponsePromise setSuccess(DirectByteInputStream result) {
		result.close();
		super.setSuccess(result);
		return this;
	}

	@Override public ResponsePromise setFailure(Throwable cause) {
		super.setFailure(cause);
		// TODO: should close result stream??
		return this;
	}

	@Override public ResponsePromise addListener(
			GenericFutureListener<? extends Future<? super DirectByteInputStream>> listener) {
		super.addListener(listener);
		return this;
	}

	@Override public ResponsePromise addListeners(
			GenericFutureListener<? extends Future<? super DirectByteInputStream>>[] listeners) {
		super.addListeners(listeners);
		return this;
	}

	@Override public ResponsePromise removeListener(
			GenericFutureListener<? extends Future<? super DirectByteInputStream>> listener) {
		super.removeListener(listener);
		return this;
	}

	@Override public ResponsePromise removeListeners(
			GenericFutureListener<? extends Future<? super DirectByteInputStream>>[] listeners) {
		super.removeListeners(listeners);
		return this;
	}

	@Override public ResponsePromise await() throws InterruptedException {
		super.await();
		return this;
	}

	@Override public ResponsePromise awaitUninterruptibly() {
		super.awaitUninterruptibly();
		return this;
	}

	@Override public ResponsePromise sync() throws InterruptedException {
		super.sync();
		return this;
	}

	@Override public ResponsePromise syncUninterruptibly() {
		super.syncUninterruptibly();
		return this;
	}
}
