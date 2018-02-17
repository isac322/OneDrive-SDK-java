package com.bhyoo.onedrive.network;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/misc/errors.htm">explain of error types</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ErrorResponse {
	@Getter protected final @NotNull String code;
	@Getter protected final @NotNull String message;
	@Getter protected final @NotNull String requestId;
	@Getter protected final @NotNull String date;

	protected ErrorResponse(@NotNull String code, @NotNull String message,
							@NotNull String requestId, @NotNull String date) {
		this.code = code;
		this.message = message;
		this.requestId = requestId;
		this.date = date;
	}

	public static @NotNull ErrorResponse deserialize(@NotNull JsonParser parser, boolean autoClose)
			throws IOException {
		@Nullable String code = null;
		@Nullable String message = null;
		@Nullable String requestId = null;
		@Nullable String date = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "error":
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						String fieldName = parser.currentName();
						parser.nextToken();

						switch (fieldName) {
							case "code":
								code = parser.getText();
								break;
							case "message":
								message = parser.getText();
								break;
							case "innerError":
								// TODO
								break;
							case "request-id":
								requestId = parser.getText();
								break;
							case "date":
								date = parser.getText();
								break;
							default:
								throw new IllegalStateException(String.format(
										"Unknown attribute detected in inner AuthenticationInfo : %s(%s)",
										fieldName, parser.getText()));
						}
					}
					break;
				default:
					throw new IllegalStateException(String.format(
							"Unknown attribute detected in AuthenticationInfo : %s(%s)",
							currentName, parser.getText()));
			}
		}

		if (autoClose) parser.close();

		assert code != null : "code is null";
		assert message != null : "message is null";
		assert requestId != null : "requestId is null";
		assert date != null : "date is null";

		return new ErrorResponse(code, message, requestId, date);
	}
}
