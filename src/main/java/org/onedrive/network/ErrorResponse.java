package org.onedrive.network;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * <a href="https://dev.onedrive.com/misc/errors.htm">explain of error types</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonRootName(value = "error")
public class ErrorResponse {
	@Getter @NotNull protected final String code;
	@Getter @NotNull protected final String message;

	@JsonCreator
	protected ErrorResponse(@NotNull @JsonProperty("code") String code,
							@NotNull @JsonProperty("message") String message) {
		this.code = code;
		this.message = message;
	}
}
