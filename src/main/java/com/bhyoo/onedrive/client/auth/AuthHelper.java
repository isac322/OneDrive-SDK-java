package com.bhyoo.onedrive.client.auth;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.sync.SyncRequest;
import com.bhyoo.onedrive.network.sync.SyncResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED;
import static java.net.HttpURLConnection.HTTP_OK;

public class AuthHelper implements AbstractAuthHelper {
	private static final String AUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0";
	private static final IllegalStateException LOGIN_FIRST = new IllegalStateException("Do login first!!");
	@NotNull private final RequestTool requestTool;
	@Getter(onMethod = @__(@Override)) @NotNull private final String[] scopes;
	@Getter(onMethod = @__(@Override)) @NotNull private final String clientId;
	@Getter(onMethod = @__(@Override)) @NotNull private final String clientSecret;
	@Getter(onMethod = @__(@Override)) @NotNull private final String redirectURL;
	@Nullable private String authCode;
	@Nullable private String fullToken;
	@Nullable private AuthenticationInfo authInfo;


	public AuthHelper(@NotNull String[] scopes, @NotNull String clientId, @NotNull String clientSecret,
					  @NotNull String redirectURL, @NotNull RequestTool requestTool) {
		this.scopes = scopes;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectURL = redirectURL;
		this.requestTool = requestTool;
	}


	@Override public boolean isLogin() {
		return authCode != null && authInfo != null;
	}


	@Override public boolean isExpired() {
		if (!isLogin()) throw LOGIN_FIRST;
		return System.currentTimeMillis() >= authInfo.getExpiresIn();
	}

	@Override public @NotNull String getTokenType() {
		checkExpired();
		return authInfo.getTokenType();
	}

	@Override public @NotNull String getAccessToken() {
		checkExpired();
		return authInfo.getAccessToken();
	}

	@Override public @NotNull String getRefreshToken() {
		checkExpired();
		return authInfo.getRefreshToken();
	}

	@Override public @NotNull String getAuthCode() {
		checkExpired();
		//noinspection ConstantConditions
		return authCode;
	}

	@Override public long getExpirationTime() {
		checkExpired();
		return authInfo.getExpiresIn();
	}

	@Override public @NotNull String getFullToken() {
		checkExpired();
		//noinspection ConstantConditions
		return fullToken;
	}


	/**
	 * Implementation of
	 * <a href=https://dev.onedrive.com/auth/msa_oauth.htm>detail</a>
	 *
	 * @throws InternalException             If fail to create {@link URI} object in auth process. or the underlying
	 *                                       input source has problems during parsing response body.
	 *                                       if it happens it's probably bug, so please report to
	 *                                       <a href="mailto:bh322yoo@gmail.com" target="_top">author</a>.
	 * @throws UnsupportedOperationException If the user default browser is not found, or it fails to be launched, or
	 *                                       the default handler application failed to be launched, or the current
	 *                                       platform does not support the {@link java.awt.Desktop.Action#BROWSE}
	 *                                       action.
	 * @throws InvalidJsonException          If fail to parse response of login request into json, or even if success
	 *                                       to parse, if there're unexpected value. both caused by server side not by
	 *                                       SDK.
	 * @throws RuntimeException              if login is unsuccessful.
	 */
	@Override public void login() {
		if (!isLogin()) {
			authCode = getCode();
			redeemToken();
		}
	}

	// TODO: Enhance javadoc

	/**
	 * @throws ErrorResponseException if raises error while logout.
	 */
	@Override public void logout() throws ErrorResponseException {
		String url = AUTH_URL + "/logout"; // AUTH_URL + "/logout?post_logout_redirect_uri=" + redirectURL;

		SyncResponse response = new SyncRequest(url).doGet();

		// FIXME: is it valid codes?
		if (response.getCode() != HttpsURLConnection.HTTP_OK) {
			String[] split = response.getUrl().getRef().split("&");
			throw new ErrorResponseException(
					HttpsURLConnection.HTTP_OK,
					response.getCode(),
					split[0].substring(split[0].indexOf('=') + 1),
					QueryStringDecoder.decodeComponent(split[1].substring(split[1].indexOf('=') + 1)));
		}

		authCode = null;
		fullToken = null;
		authInfo = null;
	}


	/**
	 * Refresh login info (same as access token).<br>
	 * <a href="https://dev.onedrive.com/auth/msa_oauth.htm#step-3-get-a-new-access-token-or-refresh-token">detail</a>
	 *
	 * @return refreshed access token {@code String}.
	 *
	 * @throws IllegalStateException If caller {@code Client} object isn't login yet.
	 * @throws InvalidJsonException  If fail to parse response of login request into json, or even if success to parse,
	 *                               if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException     if the underlying input source has problems during parsing response body.
	 */
	@Override public @NotNull String refreshLogin() {
		if (!isLogin()) throw LOGIN_FIRST;

		return getToken(
				String.format("client_id=%s&redirect_uri=%s&client_secret=%s&refresh_token=%s&grant_type" +
								"=refresh_token",
						clientId, redirectURL, clientSecret, authInfo.getRefreshToken()));
	}


	/**
	 * Implementation of
	 * <a href=https://dev.onedrive.com/auth/msa_oauth.htm#step-1-get-an-authorization-code>detail</a>.<br>
	 * Trying to login and get <a href="https://dev.onedrive.com/auth/msa_oauth.htm#code-flow">accessCode</a> from
	 * server with login information that given when constructing (see
	 * {@link Client#Client(String, String[], String, String)}.)
	 *
	 * @return <b>Access Code</b>({@code this.accessToken}) if successful. Otherwise throw {@link RuntimeException}.
	 *
	 * @throws InternalException             If fail to create {@link URI} object in auth process.
	 *                                       if it happens it's probably bug, so please report to
	 *                                       <a href="mailto:bh322yoo@gmail.com" target="_top">author</a>.
	 * @throws UnsupportedOperationException If the user default browser is not found, or it fails to be launched, or
	 *                                       the default handler application failed to be launched, or the current
	 *                                       platform does not support the {@link java.awt.Desktop.Action#BROWSE}
	 *                                       action.
	 * @throws RuntimeException              if getting <b>Access Code</b> is unsuccessful.
	 */
	private @NotNull String getCode() {
		StringBuilder scope = new StringBuilder();
		for (String s : scopes) scope.append("%20").append(s);

		String url = String.format(AUTH_URL + "/authorize?client_id=%s&scope=%s&response_type=code&redirect_uri=%s",
				clientId, scope.toString(), redirectURL)
				.replace(" ", "%20");

		Semaphore answerLock = new Semaphore(1);

		AuthServer server = new AuthServer(answerLock);
		server.start();

		try {
			Desktop.getDesktop().browse(new URI(url));
		}
		catch (URISyntaxException e) {
			throw new InternalException(
					"Fail to create URI object. probably wrong url on SDK code, contact the author", e);
		}
		catch (IOException e) {
			throw new UnsupportedOperationException("Can not find default browser for authentication.", e);
		}

		try {
			answerLock.acquire();
		}
		catch (InterruptedException e) {
			// FIXME: custom exception
			throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG + " Lock Error In " + this.getClass().getName());
		}

		String code = server.close();
		answerLock.release();

		if (code == null) {
			// FIXME: custom exception
			throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG);
		}

		return code;
	}


	/**
	 * Get token from server with login information that given when {@code Client} object was constructed.<br>
	 * And save to their own {@code Client} object.
	 * <a href="https://dev.onedrive.com/auth/msa_oauth.htm#step-3-get-a-new-access-token-or-refresh-token">detail</a>
	 *
	 * @return access token {@code String} that given from server.
	 *
	 * @throws InvalidJsonException If fail to parse response of login request into json, or even if success to parse,
	 *                              if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException    if the underlying input source has problems during parsing response body.
	 */
	private @NotNull String redeemToken() {
		return getToken(
				String.format("client_id=%s&redirect_uri=%s&client_secret=%s&code=%s&grant_type=authorization_code",
						clientId, redirectURL, clientSecret, authCode));
	}

	/**
	 * Posting login information to server, be granted and get access token from server. and save them to this
	 * {@code Client} object.
	 *
	 * @param httpBody HTTP POST's body that will be sent to server for being granted.
	 *
	 * @return access token {@code String} that given from server.
	 *
	 * @throws InvalidJsonException If fail to parse response of login request into json, or even if success to parse,
	 *                              if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException    if the underlying input source has problems during parsing response body.
	 */
	private @NotNull String getToken(String httpBody) {
		SyncResponse response = new SyncRequest(AUTH_URL + "/token")
				.setHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
				.doPost(httpBody);

		try {
			authInfo = requestTool.parseAuthAndHandle(response, HTTP_OK);
		}
		catch (ErrorResponseException e) {
			throw new InternalException("failed to acquire login token. check login info", e);
		}

		this.fullToken = authInfo.getTokenType() + ' ' + authInfo.getAccessToken();

		return authInfo.getAccessToken();
	}

	/**
	 * Check expiration of authentication. if expired, refresh it with {@link Client#refreshLogin()}.
	 *
	 * @throws IllegalStateException If caller {@code Client} object isn't login yet.
	 * @throws InvalidJsonException  If fail to parse response of login request into json, or even if success to parse,
	 *                               if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException     if the underlying input source has problems during parsing response body.
	 */
	public void checkExpired() {
		if (!isLogin()) throw LOGIN_FIRST;

		if (isExpired()) refreshLogin();
	}

}
