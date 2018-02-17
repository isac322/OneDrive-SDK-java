package com.bhyoo.onedrive.client.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;


/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AuthenticationInfo {
	@Getter @JsonProperty("token_type") protected @NotNull String tokenType;
	@Getter @JsonProperty("expires_in") protected long expiresIn;
	@Getter @JsonProperty("access_token") protected @NotNull String accessToken;
	@Getter @JsonProperty("refresh_token") protected @NotNull String refreshToken;
	@Getter protected String scope;

	protected AuthenticationInfo(@NotNull String tokenType, long expiresIn, @NotNull String accessToken,
								 @NotNull String refreshToken, @NotNull String scope) {
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.scope = scope;
	}

	public static @NotNull AuthenticationInfo deserialize(@NotNull JsonParser parser, boolean autoClose)
			throws IOException {
		@Nullable String tokenType = null;
		@Nullable Long expiresIn = null;
		@Nullable String accessToken = null;
		@Nullable String refreshToken = null;
		@Nullable String scope = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "token_type":
					tokenType = parser.getText();
					break;
				case "expires_in":
					expiresIn = parser.getLongValue() * 1000 + System.currentTimeMillis();
					break;
				case "access_token":
					accessToken = parser.getText();
					break;
				case "refresh_token":
					refreshToken = parser.getText();
					break;
				case "scope":
					scope = parser.getText();
					break;
				case "ext_expires_in":
					// TODO
					break;
				default:
					throw new IllegalStateException(
							"Unknown attribute detected in AuthenticationInfo : " + currentName);
			}
		}

		if (autoClose) parser.close();

		assert tokenType != null;
		assert expiresIn != null;
		assert accessToken != null;
		assert refreshToken != null;
		assert scope != null;

		return new AuthenticationInfo(tokenType, expiresIn, accessToken, refreshToken, scope);
	}
}
