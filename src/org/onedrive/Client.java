package org.onedrive;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.container.Drive;
import org.onedrive.container.items.BaseItem;
import org.onedrive.container.items.FileItem;
import org.onedrive.container.items.FolderItem;
import org.onedrive.network.legacy.HttpsRequest;
import org.onedrive.network.legacy.HttpsResponse;
import org.onedrive.utils.AuthServer;
import org.onedrive.utils.OneDriveRequest;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * {@// TODO: add javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class Client {
	public static final ExecutorService threadPool =
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	/*
	for resolve tricky issue of Jackson.
	See:
	https://github.com/FasterXML/jackson-databind/issues/1119
	and
	https://github.com/FasterXML/jackson-databind/issues/962
	*/
	private static final SimpleModule jacksonIsTricky =
			new SimpleModule().setMixInAnnotation(ObjectMapper.class, Client.IgnoreMe.class);
	/**
	 * Only one {@code mapper} per a {@code Client} object.<br>
	 * It makes possible to multi client usage
	 */
	@Getter private final ObjectMapper mapper;
	@Getter @NotNull private final OneDriveRequest requestTool;

	private long lastRefresh;
	@Getter private long expirationTime;
	@Nullable private String authCode;
	@Nullable private String tokenType;
	@Nullable private String accessToken;
	@Nullable private String userId;
	@Nullable private String refreshToken;
	@Nullable private String fullToken;
	@Getter @NotNull private String[] scopes;
	@Getter @NotNull private String clientId;
	@Getter @NotNull private String clientSecret;
	@Getter @NotNull private String redirectURL;

	/**
	 * Construct with auto login.
	 *
	 * @param clientId     Client id that MS gave to programmer for identify programmer's applications.
	 * @param scope        Array of scopes that client requires.
	 * @param redirectURL  Redirect URL that programmer already set in Application setting. It must matches with set
	 *                     one!
	 * @param clientSecret Client secret key that MS gave to programmer.
	 */
	public Client(@NotNull String clientId, @NotNull String[] scope,
				  @NotNull String redirectURL, @NotNull String clientSecret) {
		this(clientId, scope, redirectURL, clientSecret, true);
	}

	/**
	 * @param clientId     Client id that MS gave to programmer for identify programmer's applications.
	 * @param scope        Array of scopes that client requires.
	 * @param redirectURL  Redirect URL that programmer already set in Application setting. It must matches with set
	 *                     one!
	 * @param clientSecret Client secret key that MS gave to programmer.
	 * @param autoLogin    if {@code true} construct with auto login.
	 */
	public Client(@NotNull String clientId, @NotNull String[] scope, @NotNull String redirectURL,
				  @NotNull String clientSecret, boolean autoLogin) {
		this.scopes = scope;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectURL = redirectURL;

		mapper = new ObjectMapper();

		InjectableValues.Std clientInjector = new InjectableValues.Std().addValue("OneDriveClient", this);
		mapper.setInjectableValues(clientInjector);

		/*
		for resolve tricky issue of Jackson.
		See:
		https://github.com/FasterXML/jackson-databind/issues/1119
		and
		https://github.com/FasterXML/jackson-databind/issues/962
		 */
		mapper.registerModule(jacksonIsTricky);
		mapper.registerModule(new AfterburnerModule());


		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// in serialization, ignore null values.
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		requestTool = new OneDriveRequest(this, mapper);

		if (autoLogin) login();
	}

	/**
	 * Implementation of
	 * <a href=https://dev.onedrive.com/auth/msa_oauth.htm>detail</a>
	 *
	 * @throws RuntimeException if login is unsuccessful.
	 */
	private void login() throws RuntimeException {
		if (!isLogin()) {
			authCode = getCode();
			redeemToken();
		}
	}

	/**
	 * Implementation of
	 * <a href=https://dev.onedrive.com/auth/msa_oauth.htm#step-1-get-an-authorization-code>detail</a>
	 *
	 * @return <b>Access Code</b>({@code this.accessToken}) if successful. Otherwise throw {@link RuntimeException}.
	 * @throws RuntimeException if getting <b>Access Code</b> is unsuccessful.
	 */
	@NotNull
	private String getCode() {
		String scope = "";
		for (String s : scopes) scope += "%20" + s;

		String url = String.format("https://login.live.com/oauth20_authorize.srf" +
				"?client_id=%s&scope=%s&response_type=code&redirect_uri=%s", clientId, scope, redirectURL)
				.replace(" ", "%20");

		try {
			Semaphore answerLock = new Semaphore(1);

			AuthServer server = new AuthServer(answerLock);
			server.start();
			Desktop.getDesktop().browse(new URI(url));

			answerLock.acquire();
			String code = server.close();
			answerLock.release();

			if (code == null) {
				throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
			}

			return code;
		}
		catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Lock Error In " + this.getClass().getName());
		}
	}

	@NotNull
	private String redeemToken() {
		return getToken(
				String.format("client_id=%s&redirect_uri=%s&client_secret=%s&code=%s&grant_type=authorization_code",
						clientId, redirectURL, clientSecret, authCode));
	}

	@NotNull
	public String refreshToken() {
		if (!isLogin()) {
			throw new RuntimeException("Do login first!!");
		}

		return getToken(
				String.format("client_id=%s&redirect_uri=%s&client_secret=%s&refresh_token=%s&grant_type" +
								"=refresh_token",
						clientId, redirectURL, clientSecret, refreshToken));
	}

	@NotNull
	private String getToken(String httpBody) {
		try {
			HttpsRequest request = new HttpsRequest("https://login.live.com/oauth20_token.srf");
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");

			HttpsResponse response = request.doPost(httpBody);

			JsonNode json = mapper.readTree(response.getContent());

			JsonNode access = json.get("access_token");
			JsonNode refresh = json.get("refresh_token");
			JsonNode id = json.get("user_id");
			JsonNode type = json.get("token_type");
			JsonNode expires = json.get("expires_in");

			if (access == null || refresh == null || id == null || type == null || expires == null) {
				throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
			}

			saveToken(
					access.asText(),
					refresh.asText(),
					id.asText(),
					type.asText(),
					expires.asLong()
			);

			return access.asText();

		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					HttpsRequest.NETWORK_ERR_MSG + " Internet Connection Error While Refreshing Login Info");
		}
	}

	private void saveToken(String accessToken, String refreshToken, String userId, String type, long expirationTime) {
		this.tokenType = type;
		this.userId = userId;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expirationTime = expirationTime * 1000;
		this.lastRefresh = System.currentTimeMillis();
		this.fullToken = type + ' ' + accessToken;
	}

	private void checkExpired() {
		if (!isLogin()) {
			throw new RuntimeException("Do login first!!");
		}

		if (isExpired()) {
			refreshToken();
		}
	}

	/**
	 * {@// TODO: check logout HTTP response about error.}
	 *
	 * @throws RuntimeException if it isn't login when called.
	 */
	@SneakyThrows(MalformedURLException.class)
	public void logout() {
		if (!isLogin()) throw new RuntimeException("Already Logout!!");

		String url = String.format("https://login.live.com/oauth20_logout.srf?client_id=%s&redirect_uri=%s",
				clientId, redirectURL);

		HttpsResponse response = new HttpsRequest(url).doGet();

		if (response.getCode() != HttpsURLConnection.HTTP_MOVED_TEMP) {
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Fail to logout.");
		}

		authCode = null;
		accessToken = null;
		userId = null;
		refreshToken = null;
		expirationTime = 0;
		fullToken = null;
	}

	@NotNull
	public Drive getDefaultDrive() {
		checkExpired();

		return requestTool.doGetObject("/drive", Drive.class);
	}

	@NotNull
	public Drive[] getAllDrive() {
		checkExpired();

		ObjectNode jsonResponse = requestTool.doGetJson("/drives");

		return mapper.convertValue(jsonResponse.get("value"), Drive[].class);
	}

	@NotNull
	public FolderItem getRootDir() {
		checkExpired();

		return requestTool.doGetObject("/drive/root:/?expand=children", FolderItem.class);
	}

	/**
	 * {@// TODO: handling error if `id`'s item isn't folder item. }
	 *
	 * @param id folder id.
	 * @return folder object
	 */
	@NotNull
	public FolderItem getFolder(@NotNull String id) {
		checkExpired();

		return requestTool.doGetObject("/drive/items/" + id + "?expand=children", FolderItem.class);
	}

	/**
	 * {@// TODO: handling error if `id`'s item isn't folder item. }
	 *
	 * @param id file id.
	 * @return file object
	 */
	@NotNull
	public FileItem getFile(@NotNull String id) {
		checkExpired();

		return requestTool.doGetObject("/drive/items/" + id, FileItem.class);
	}

	@NotNull
	public BaseItem getItem(@NotNull String id) {
		checkExpired();

		return requestTool.doGetObject("/drive/items/" + id, BaseItem.class);
	}

	@NotNull
	public BaseItem[] getShared() {
		checkExpired();

		ArrayNode values = (ArrayNode) requestTool.doGetJson("/drive/shared").get("value");

		int size = values.size();
		BaseItem[] items = new BaseItem[size];


		ObjectNode jsonResponse;
		for (int i = 0; i < size; i++) {
			jsonResponse = requestTool.doGetJson(
					"/drive/items/" + values.get(i).get("id").asText() + "?expand=children"
			);

			items[i] = mapper.convertValue(jsonResponse, BaseItem.class);
		}

		return items;
	}


	/*
	=============================================================
	Custom Getter
	=============================================================
	 */


	public boolean isExpired() {
		return System.currentTimeMillis() - lastRefresh >= expirationTime;
	}

	public boolean isLogin() {
		return authCode != null && accessToken != null && userId != null && refreshToken != null;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public String getTokenType() {
		checkExpired();
		return tokenType;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public String getAccessToken() {
		checkExpired();
		return accessToken;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public String getUserId() {
		checkExpired();
		return userId;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public String getRefreshToken() {
		checkExpired();
		return refreshToken;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public String getAuthCode() {
		checkExpired();
		return authCode;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public String getFullToken() {
		checkExpired();
		return fullToken;
	}


	@JsonIgnoreType
	private static class IgnoreMe {
	}
}
