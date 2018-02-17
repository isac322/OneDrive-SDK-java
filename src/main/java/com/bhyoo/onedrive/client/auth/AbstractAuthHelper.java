package com.bhyoo.onedrive.client.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

public interface AbstractAuthHelper {
	@NotNull String refreshLogin();

	void logout();

	void login();

	@JsonIgnore boolean isExpired();

	@JsonIgnore boolean isLogin();

	@JsonIgnore @NotNull String getTokenType();

	@JsonIgnore @NotNull String getAccessToken();

	@JsonIgnore @NotNull String getRefreshToken();

	@JsonIgnore @NotNull String getAuthCode();

	@JsonIgnore @NotNull String[] getScopes();

	@JsonIgnore @NotNull String getClientId();

	@JsonIgnore @NotNull String getClientSecret();

	@JsonIgnore @NotNull String getRedirectURL();

	@JsonIgnore long getExpirationTime();

	@JsonIgnore @NotNull String getFullToken();
}
