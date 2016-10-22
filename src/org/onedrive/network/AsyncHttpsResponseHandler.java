package org.onedrive.network;

import io.netty.handler.codec.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public interface AsyncHttpsResponseHandler {
	void handle(@NotNull InputStream resultStream, @NotNull HttpResponse response);
}
