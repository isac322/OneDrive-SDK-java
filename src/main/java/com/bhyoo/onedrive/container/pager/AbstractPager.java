package com.bhyoo.onedrive.container.pager;

import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.netty.handler.codec.http.HttpMethod.GET;

abstract public class AbstractPager<T> implements Iterable<T> {
	protected final @NotNull RequestTool requestTool;
	protected @NotNull Page<T> page;

	protected AbstractPager(@NotNull RequestTool requestTool, @NotNull Page<T> page) {
		this.requestTool = requestTool;
		this.page = page;
	}


	abstract static class Page<T> {
		@Getter protected final @Nullable URI nextLink;
		@Getter protected final @Nullable URI deltaLink;
		@Getter protected final @NotNull T value;

		Page(@Nullable URI nextLink, @Nullable URI deltaLink, @NotNull T value) {
			this.nextLink = nextLink;
			this.deltaLink = deltaLink;
			this.value = value;
		}
	}


	abstract static class PageIterator<T> implements Iterator<T> {
		protected final @NotNull RequestTool requestTool;
		protected @Nullable Page<T> currentPage;
		protected boolean isFirst = true;

		PageIterator(@NotNull RequestTool requestTool, @NotNull Page<T> currentPage) {
			this.requestTool = requestTool;
			this.currentPage = currentPage;
		}

		@Override public T next() {
			if (isFirst) {
				isFirst = false;
				assert currentPage != null : "currentPage is null";
				return currentPage.value;
			}
			else if (currentPage == null || currentPage.nextLink == null) throw new NoSuchElementException();
			else {
				ResponseFuture responseFuture = requestTool.doAsync(GET, currentPage.nextLink).syncUninterruptibly();
				try {
					currentPage = parse(responseFuture);
					return currentPage.value;
				}
				catch (ErrorResponseException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		@Override public boolean hasNext() {return currentPage != null && currentPage.nextLink != null;}

		@Override public void remove() {throw new UnsupportedOperationException();}

		protected abstract Page<T> parse(@NotNull ResponseFuture responseFuture) throws ErrorResponseException;
	}
}
