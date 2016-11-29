package org.onedrive.network.async;

import io.netty.handler.codec.http.HttpResponse;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.utils.DirectByteInputStream;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public interface AsyncResponseHandler {
	void handle(DirectByteInputStream result, HttpResponse response) throws ErrorResponseException;
}
