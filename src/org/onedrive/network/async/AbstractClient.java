package org.onedrive.network.async;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.Future;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.utils.RequestTool;

import java.net.URI;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public abstract class AbstractClient {
	@NotNull @Getter protected final URI uri;
	@NotNull @Getter protected final HttpMethod method;
	@Nullable @Getter protected final byte[] content;
	@NotNull protected final DefaultFullHttpRequest request;


	public AbstractClient(@NotNull HttpMethod method, @NotNull URI uri, @Nullable byte[] content) {
		this.uri = uri;
		this.method = method;
		this.content = content;

		if (!RequestTool.SCHEME.equalsIgnoreCase(uri.getScheme())) {
			throw new IllegalArgumentException("Wrong network scheme : \"" + uri.getScheme() + "\".");
		}

		if (content != null) {
			this.request = new DefaultFullHttpRequest(
					HttpVersion.HTTP_1_1,
					method,
					uri.toASCIIString(),
					Unpooled.wrappedBuffer(content));
		}
		else {
			this.request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri.toASCIIString());
		}

		this.request.headers().set(HttpHeaderNames.HOST, uri.getHost());
		this.request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);
	}

	@NotNull public AbstractClient setHeader(AsciiString header, CharSequence value) {
		request.headers().set(header, value);
		return this;
	}

	@NotNull public AbstractClient setHeader(String header, String value) {
		request.headers().set(header, value);
		return this;
	}

	public abstract Future<?> execute();
}
