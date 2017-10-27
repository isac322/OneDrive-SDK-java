package com.bhyoo.onedrive.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

// TODO: Enhance javadoc

/**
 * implements of <a href='https://dev.onedrive.com/resources/uploadSession.htm'>detail</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class UploadSession {
	@Getter final String uploadUrl;
	@Getter String expirationDateTime;
	@Getter String[] nextExpectedRanges;


	@JsonCreator
	public UploadSession(@NotNull @JsonProperty("uploadUrl") String uploadUrl,
						 @NotNull @JsonProperty("expirationDateTime") String expirationDateTime,
						 @NotNull @JsonProperty("nextExpectedRanges") String[] nextExpectedRanges) {
		this.uploadUrl = uploadUrl;
		this.expirationDateTime = expirationDateTime;
		this.nextExpectedRanges = nextExpectedRanges;
	}
}
