package com.bhyoo.onedrive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AuthenticationInfo {
	@Getter @Setter(PRIVATE) @JsonProperty("token_type") private String tokenType;
	@Getter @JsonProperty("expires_in") private long expiresIn;
	@Getter @Setter(PRIVATE) @JsonProperty("access_token") private String accessToken;
	@Getter @Setter(PRIVATE) @JsonProperty("refresh_token") private String refreshToken;
	@Getter @Setter(PRIVATE) private String scope;

	private void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn * 1000 + System.currentTimeMillis();
	}
}
