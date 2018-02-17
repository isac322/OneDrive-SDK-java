package com.bhyoo.onedrive.network;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * implements of <a href='https://dev.onedrive.com/resources/uploadSession.htm'>detail</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class UploadSession {
	@Getter protected @Nullable String uploadUrl;
	@Getter protected @NotNull String expirationDateTime;
	@Getter protected @NotNull String[] nextExpectedRanges;


	UploadSession(@Nullable String uploadUrl, @NotNull String expirationDateTime,
				  @NotNull String[] nextExpectedRanges) {
		this.uploadUrl = uploadUrl;
		this.expirationDateTime = expirationDateTime;
		this.nextExpectedRanges = nextExpectedRanges;
	}

	public static @NotNull UploadSession deserialize(@NotNull JsonParser parser, boolean autoClose)
			throws IOException {
		@Nullable String uploadUrl = null;
		@Nullable String expirationDateTime = null;
		@Nullable String[] nextExpectedRanges = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "uploadUrl":
					uploadUrl = parser.getText();
					break;
				case "expirationDateTime":
					expirationDateTime = parser.getText();
					break;
				case "nextExpectedRanges":
					ArrayList<String> ranges = new ArrayList<>();
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						ranges.add(parser.getText());
					}
					nextExpectedRanges = ranges.toArray(new String[0]);
					break;

				default:
					throw new IllegalStateException("Unknown attribute detected in UploadSession : " + currentName);
			}
		}

		if (autoClose) parser.close();

		assert uploadUrl != null;
		assert expirationDateTime != null;
		assert nextExpectedRanges != null;

		return new UploadSession(uploadUrl, expirationDateTime, nextExpectedRanges);
	}
}
