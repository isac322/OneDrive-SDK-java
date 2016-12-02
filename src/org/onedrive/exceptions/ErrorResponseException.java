package org.onedrive.exceptions;

import lombok.Getter;

/**
 * {@// TODO: Enhance javadoc }
 * Exception that server rejects to response.<br>
 * Mostly because of invalid request, such as requesting deleting item that already deleted.<br>
 * More details of error type can be found <a href="https://dev.onedrive.com/misc/errors.htm">here</a>.
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ErrorResponseException extends Exception implements OneDriveSDKException {
	@Getter private final int expectedResponse, givenResponse;
	@Getter private final String errorCode, errorMessage;

	public ErrorResponseException(int expectedResponse, int givenResponse, String errorCode, String errorMessage) {
		super(String.format("Expected %d response code, but received %d. It means %s (%s).",
				expectedResponse, givenResponse, errorCode, errorMessage));
		this.expectedResponse = expectedResponse;
		this.givenResponse = givenResponse;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
