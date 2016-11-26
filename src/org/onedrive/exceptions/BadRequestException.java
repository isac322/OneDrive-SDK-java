package org.onedrive.exceptions;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class BadRequestException extends Exception implements OneDriveSDKException {
	public BadRequestException(String message) {
		super(message);
	}
}
