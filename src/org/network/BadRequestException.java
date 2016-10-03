package org.network;

import java.io.IOException;

/**
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 4.
 *
 * @author isac322
 */
public class BadRequestException extends IOException {
	public BadRequestException(String message) {
		super(message);
	}
}
