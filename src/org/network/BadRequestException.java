package org.network;

import java.io.IOException;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class BadRequestException extends IOException {
	public BadRequestException(String message) {
		super(message);
	}
}
