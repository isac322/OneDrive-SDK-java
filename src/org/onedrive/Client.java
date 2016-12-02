package org.onedrive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.container.Drive;
import org.onedrive.container.items.BaseItem;
import org.onedrive.container.items.FileItem;
import org.onedrive.container.items.FolderItem;
import org.onedrive.container.items.pointer.BasePointer;
import org.onedrive.container.items.pointer.Operator;
import org.onedrive.container.items.pointer.PathPointer;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.exceptions.InternalException;
import org.onedrive.exceptions.InvalidJsonException;
import org.onedrive.network.async.AsyncDownloadClient;
import org.onedrive.network.async.DownloadFuture;
import org.onedrive.network.async.ResponseFuture;
import org.onedrive.network.async.ResponseFutureListener;
import org.onedrive.network.sync.SyncRequest;
import org.onedrive.network.sync.SyncResponse;
import org.onedrive.utils.AuthServer;
import org.onedrive.utils.RequestTool;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class Client {
	public static final String ITEM_ID_PREFIX = "/drive/items/";
	@NotNull private static final IllegalStateException LOGIN_FIRST = new IllegalStateException("Do login first!!");

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
	private final ObjectMapper mapper;
	@NotNull private final RequestTool requestTool;

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
	 * @throws InternalException             If fail to create {@link URI} object in auth process.
	 *                                       if it happens it's probably bug, so please report to
	 *                                       <a href="mailto:yoobyeonghun@gmail.com" target="_top">author</a>.
	 * @throws UnsupportedOperationException If the user default browser is not found, or it fails to be launched, or
	 *                                       the default handler application failed to be launched, or the current
	 *                                       platform does not support the {@link java.awt.Desktop.Action#BROWSE}
	 *                                       action.
	 * @throws RuntimeException              if login is unsuccessful.
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
	 * @throws InternalException             If fail to create {@link URI} object in auth process.
	 *                                       if it happens it's probably bug, so please report to
	 *                                       <a href="mailto:yoobyeonghun@gmail.com" target="_top">author</a>.
	 * @throws UnsupportedOperationException If the user default browser is not found, or it fails to be launched, or
	 *                                       the default handler application failed to be launched, or the current
	 *                                       platform does not support the {@link java.awt.Desktop.Action#BROWSE}
	 *                                       action.
	 * @throws RuntimeException              if login is unsuccessful.
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

		requestTool = new RequestTool(this, mapper);

		if (autoLogin) login();
	}

	/**
	 * Implementation of
	 * <a href=https://dev.onedrive.com/auth/msa_oauth.htm>detail</a>
	 *
	 * @throws InternalException             If fail to create {@link URI} object in auth process. or the underlying
	 *                                       input source has problems during parsing response body.
	 *                                       if it happens it's probably bug, so please report to
	 *                                       <a href="mailto:yoobyeonghun@gmail.com" target="_top">author</a>.
	 * @throws UnsupportedOperationException If the user default browser is not found, or it fails to be launched, or
	 *                                       the default handler application failed to be launched, or the current
	 *                                       platform does not support the {@link java.awt.Desktop.Action#BROWSE}
	 *                                       action.
	 * @throws InvalidJsonException          If fail to parse response of login request into json, or even if success
	 *                                       to parse, if there're unexpected value. both caused by server side not by
	 *                                       SDK.
	 * @throws RuntimeException              if login is unsuccessful.
	 */
	public void login() {
		if (!isLogin()) {
			authCode = getCode();
			redeemToken();
		}
	}




	/*
	*************************************************************
	*
	* Regarding authorization
	*
	*************************************************************
	 */


	/**
	 * Implementation of
	 * <a href=https://dev.onedrive.com/auth/msa_oauth.htm#step-1-get-an-authorization-code>detail</a>.<br>
	 * Trying to login and get <a href="https://dev.onedrive.com/auth/msa_oauth.htm#code-flow">accessCode</a> from
	 * server with login information that given when constructing (see
	 * {@link Client#Client(String, String[], String, String)}.)
	 *
	 * @return <b>Access Code</b>({@code this.accessToken}) if successful. Otherwise throw {@link RuntimeException}.
	 * @throws InternalException             If fail to create {@link URI} object in auth process.
	 *                                       if it happens it's probably bug, so please report to
	 *                                       <a href="mailto:yoobyeonghun@gmail.com" target="_top">author</a>.
	 * @throws UnsupportedOperationException If the user default browser is not found, or it fails to be launched, or
	 *                                       the default handler application failed to be launched, or the current
	 *                                       platform does not support the {@link java.awt.Desktop.Action#BROWSE}
	 *                                       action.
	 * @throws RuntimeException              if getting <b>Access Code</b> is unsuccessful.
	 */
	@NotNull
	private String getCode() {
		String scope = "";
		for (String s : scopes) scope += "%20" + s;

		String url = String.format("https://login.live.com/oauth20_authorize.srf" +
				"?client_id=%s&scope=%s&response_type=code&redirect_uri=%s", clientId, scope, redirectURL)
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
			// TODO: custom exception
			throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG + " Lock Error In " + this.getClass().getName());
		}

		String code = server.close();
		answerLock.release();

		if (code == null) {
			// TODO: custom exception
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
	 * @throws InvalidJsonException If fail to parse response of login request into json, or even if success to parse,
	 *                              if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException    if the underlying input source has problems during parsing response body.
	 */
	@NotNull
	private String redeemToken() {
		return getToken(
				String.format("client_id=%s&redirect_uri=%s&client_secret=%s&code=%s&grant_type=authorization_code",
						clientId, redirectURL, clientSecret, authCode));
	}

	/**
	 * Refresh login info (same as access token).<br>
	 * <a href="https://dev.onedrive.com/auth/msa_oauth.htm#step-3-get-a-new-access-token-or-refresh-token">detail</a>
	 *
	 * @return refreshed access token {@code String}.
	 * @throws IllegalStateException If caller {@code Client} object isn't login yet.
	 * @throws InvalidJsonException  If fail to parse response of login request into json, or even if success to parse,
	 *                               if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException     if the underlying input source has problems during parsing response body.
	 */
	@NotNull
	public String refreshToken() {
		if (!isLogin()) throw LOGIN_FIRST;

		return getToken(
				String.format("client_id=%s&redirect_uri=%s&client_secret=%s&refresh_token=%s&grant_type" +
								"=refresh_token",
						clientId, redirectURL, clientSecret, refreshToken));
	}

	/**
	 * Posting login information to server, be granted and get access token from server. and save them to this
	 * {@code Client} object.
	 *
	 * @param httpBody HTTP POST's body that will be sent to server for being granted.
	 * @return access token {@code String} that given from server.
	 * @throws InvalidJsonException If fail to parse response of login request into json, or even if success to parse,
	 *                              if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException    if the underlying input source has problems during parsing response body.
	 */
	@NotNull
	private String getToken(String httpBody) {
		SyncResponse response = new SyncRequest("https://login.live.com/oauth20_token.srf").doPost(httpBody);

		JsonNode json;
		try {
			json = mapper.readTree(response.getContent());
		}
		catch (JsonProcessingException e) {
			throw new InvalidJsonException(e, response.getCode(), response.getContent());
		}
		catch (IOException e) {
			throw new InternalException("Probably error while read from source in json parsing", e);
		}

		JsonNode access = json.get("access_token");
		JsonNode refresh = json.get("refresh_token");
		JsonNode id = json.get("user_id");
		JsonNode type = json.get("token_type");
		JsonNode expires = json.get("expires_in");

		if (access == null || !access.isTextual() || refresh == null || !refresh.isTextual() || id == null
				|| !id.isTextual() || type == null || !type.isTextual() || expires == null
				|| !expires.isIntegralNumber()) {
			throw new InvalidJsonException(
					String.format("Null value detected: %s %s %s %s %s",
							access == null ? "access_token" : "",
							refresh == null ? "refresh_token" : "",
							id == null ? "user_id" : "",
							type == null ? "token_type" : "",
							expires == null ? "expires_in" : ""),
					response.getCode(),
					response.getContent()
			);
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

	private void saveToken(String accessToken, String refreshToken, String userId, String type, long expirationTime) {
		this.tokenType = type;
		this.userId = userId;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expirationTime = expirationTime * 1000;
		this.lastRefresh = System.currentTimeMillis();
		this.fullToken = type + ' ' + accessToken;
	}

	/**
	 * Check expiration of authentication. if expired, refresh it with {@link Client#refreshToken()}.
	 *
	 * @throws IllegalStateException If caller {@code Client} object isn't login yet.
	 * @throws InvalidJsonException  If fail to parse response of login request into json, or even if success to parse,
	 *                               if there're unexpected value. both caused by server side not by SDK.
	 * @throws InternalException     if the underlying input source has problems during parsing response body.
	 */
	private void checkExpired() {
		if (!isLogin()) throw LOGIN_FIRST;

		if (isExpired()) refreshToken();
	}


	/**
	 * {@// TODO: Enhance javadoc }
	 *
	 * @throws ErrorResponseException if raises error while logout.
	 */
	@SneakyThrows(UnsupportedEncodingException.class)
	public void logout() throws ErrorResponseException {
		String url = String.format("https://login.live.com/oauth20_logout.srf?client_id=%s&redirect_uri=%s",
				clientId, redirectURL);

		SyncResponse response = new SyncRequest(url).doGet();

		if (response.getCode() != HttpsURLConnection.HTTP_MOVED_TEMP) {
			String[] split = response.getUrl().getRef().split("&");
			throw new ErrorResponseException(
					HttpsURLConnection.HTTP_MOVED_TEMP,
					response.getCode(),
					split[0].substring(split[0].indexOf('=') + 1),
					URLDecoder.decode(split[1].substring(split[1].indexOf('=') + 1), "UTF-8"));
		}

		authCode = null;
		accessToken = null;
		userId = null;
		refreshToken = null;
		expirationTime = 0;
		fullToken = null;
	}




	/*
	*************************************************************
	*
	* Regarding drive
	*
	*************************************************************
	 */


	@NotNull
	public Drive getDefaultDrive() throws ErrorResponseException {
		checkExpired();

		SyncResponse response = requestTool.newRequest("/drive").doGet();
		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_OK, Drive.class);
	}

	@NotNull
	public Drive[] getAllDrive() throws ErrorResponseException {
		checkExpired();

		ObjectNode jsonResponse = requestTool.doGetJson("/drives");

		return mapper.convertValue(jsonResponse.get("value"), Drive[].class);
	}




	/*
	*************************************************************
	*
	* Fetching folder
	*
	*************************************************************
	 */


	@NotNull
	public FolderItem getRootDir() throws ErrorResponseException {
		checkExpired();

		SyncResponse response = requestTool.newRequest("/drive/root:/?expand=children").doGet();
		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_OK, FolderItem.class);
	}


	/**
	 * {@// TODO: Enhance javadoc }
	 * {@// TODO: handling error if `id`'s item isn't folder item. }
	 *
	 * @param id folder's id.
	 * @return folder object
	 */
	@NotNull
	public FolderItem getFolder(@NotNull String id) throws ErrorResponseException {
		return getFolder(id, true);
	}

	// TODO: handling error if `id`'s item isn't folder item.
	@NotNull
	public FolderItem getFolder(@NotNull String id, boolean childrenFetching) throws ErrorResponseException {
		checkExpired();

		SyncResponse response;

		if (childrenFetching)
			response = requestTool.newRequest(ITEM_ID_PREFIX + id + "?expand=children").doGet();
		else
			response = requestTool.newRequest(ITEM_ID_PREFIX + id).doGet();

		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_OK, FolderItem.class);
	}

	// TODO: handling error if `pointer`'s item isn't folder item.
	@NotNull
	public FolderItem getFolder(@NotNull BasePointer pointer) throws ErrorResponseException {
		return getFolder(pointer, true);
	}

	// TODO: handling error if `pointer`'s item isn't folder item.
	@NotNull
	public FolderItem getFolder(@NotNull BasePointer pointer, boolean childrenFetching) throws ErrorResponseException {
		checkExpired();

		SyncResponse response;

		if (childrenFetching)
			response = requestTool.newRequest(pointer.toASCIIApi() + "?expand=children").doGet();
		else
			response = requestTool.newRequest(pointer.toASCIIApi()).doGet();

		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_OK, FolderItem.class);
	}




	/*
	*************************************************************
	*
	* Fetching file
	*
	*************************************************************
	 */


	/**
	 * {@// TODO: Enhance javadoc }
	 *
	 * @param id file id.
	 * @return file object
	 */
	@NotNull
	public FileItem getFile(@NotNull String id) throws ErrorResponseException {
		try {
			return (FileItem) getItem(id);
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Given `id` isn't file type id!. id : " + id, e);
		}
	}

	@NotNull
	public FileItem getFile(@NotNull BasePointer pointer) throws ErrorResponseException {
		try {
			return (FileItem) getItem(pointer);
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Given `pointer` isn't file type pointer!. pointer : " + pointer, e);
		}
	}




	/*
	*************************************************************
	*
	* Fetching item
	*
	* *************************************************************
	 */


	@NotNull
	public BaseItem getItem(@NotNull String id) throws ErrorResponseException {
		checkExpired();

		SyncResponse response = requestTool.newRequest(ITEM_ID_PREFIX + id).doGet();
		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_OK, BaseItem.class);
	}

	@NotNull
	public BaseItem getItem(@NotNull BasePointer pointer) throws ErrorResponseException {
		checkExpired();

		SyncResponse response = requestTool.newRequest(pointer.toASCIIApi()).doGet();
		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_OK, BaseItem.class);
	}

	@NotNull
	public BaseItem[] getShared() throws ErrorResponseException {
		checkExpired();

		ArrayNode values = (ArrayNode) requestTool.doGetJson("/drive/shared").get("value");

		int size = values.size();
		final BaseItem[] items = new BaseItem[size];
		ResponseFuture[] handlers = new ResponseFuture[size];
		final CountDownLatch[] latches = new CountDownLatch[size];

		for (int i = 0; i < size; i++) {
			JsonNode id = values.get(i).get("id");

			// if response doesn't have `id` field or `id` isn't text
			assert !(id == null || !id.isTextual()) : i + "th shared element doesn't have `id` field";

			// for inner class access
			final int finalI = i;

			latches[i] = new CountDownLatch(1);
			handlers[i] = requestTool.doAsync(
					HttpMethod.GET,
					ITEM_ID_PREFIX + id.asText() + "?expand=children",
					new ResponseFutureListener() {
						@Override public void operationComplete(ResponseFuture future) throws Exception {
							items[finalI] = requestTool.parseAndHandle(
									future.response(),
									future.get(),
									HttpURLConnection.HTTP_OK,
									BaseItem.class);
							latches[finalI].countDown();
						}
					});
		}

		for (ResponseFuture handler : handlers) handler.syncUninterruptibly();
		for (CountDownLatch latch : latches) {
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				throw new InternalException("Exception occurs while waiting lock in Client#getShared()", e);
			}
		}

		return items;
	}




	/*
	*************************************************************
	*
	* Coping OneDrive Item
	*
	*************************************************************
	 */


	/**
	 * request to copy {@code srcId} item to new location of {@code destId}.
	 *
	 * @param srcId  item's id that wants to be copied
	 * @param destId location's id that wants to be placed the copied item
	 * @return URL {@code String} that can monitor status of copying process
	 * @throws ErrorResponseException if error happens while requesting copying operation. such as invalid copying
	 *                                request
	 * @throws InvalidJsonException   if fail to parse response of copying request into json. it caused by server side
	 *                                not by SDK.
	 */
	@NotNull
	public String copyItem(@NotNull String srcId, @NotNull String destId) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":{\"id\":\"" + destId + "\"}}").getBytes();
		return copyItem(ITEM_ID_PREFIX + srcId + "/action.copy", content);
	}

	/**
	 * Works just like {@link Client#copyItem(String, String)}} except new name of item can be designated.
	 *
	 * @param newName new name of item that will be copied
	 * @see Client#copyItem(String, String)
	 */
	@NotNull
	public String copyItem(@NotNull String srcId, @NotNull String destId, @NotNull String newName)
			throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":{\"id\":\"" + destId + "\"},\"name\":\"" + newName + "\"}").getBytes();
		return copyItem(ITEM_ID_PREFIX + srcId + "/action.copy", content);
	}

	@NotNull
	public String copyItem(@NotNull String srcId, @NotNull PathPointer destPath) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":" + destPath.toJson() + "}").getBytes();
		return copyItem(ITEM_ID_PREFIX + srcId + "/action.copy", content);
	}

	@NotNull
	public String copyItem(@NotNull String srcId, @NotNull PathPointer dest, @NotNull String newName)
			throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":" + dest.toJson() + ",\"name\":\"" + newName + "\"}").getBytes();
		return copyItem(ITEM_ID_PREFIX + srcId + "/action.copy", content);
	}

	@NotNull
	public String copyItem(@NotNull PathPointer srcPath, @NotNull String destId) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":{\"id\":\"" + destId + "\"}}").getBytes();
		return copyItem(srcPath.resolveOperator(Operator.ACTION_COPY), content);
	}

	@NotNull
	public String copyItem(@NotNull PathPointer srcPath, @NotNull String destId, @NotNull String newName)
			throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":{\"id\":\"" + destId + "\"},\"name\":\"" + newName + "\"}").getBytes();
		return copyItem(srcPath.resolveOperator(Operator.ACTION_COPY), content);
	}

	@NotNull
	public String copyItem(@NotNull BasePointer src, @NotNull BasePointer dest) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":" + dest.toJson() + "}").getBytes();
		return copyItem(src.resolveOperator(Operator.ACTION_COPY), content);
	}

	@NotNull
	public String copyItem(@NotNull BasePointer src, @NotNull BasePointer dest, @NotNull String newName)
			throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":" + dest.toJson() + ",\"name\":\"" + newName + "\"}").getBytes();
		return copyItem(src.resolveOperator(Operator.ACTION_COPY), content);
	}


	/**
	 * @param api     OneDrive copying api that contains item's location. Note that it must be ensured that
	 *                {@code api} is a escaped {@code String}
	 * @param content HTTP body
	 * @return URL {@code String} that can monitor status of copying process
	 * {@// TODO: end of copying process, is this link will be useless or inaccessible ? }
	 * @throws ErrorResponseException if error happens while requesting copying operation. such as invalid copying
	 *                                request
	 * @throws InvalidJsonException   if fail to parse response of copying request into json. it caused by server side
	 *                                not by SDK.
	 */
	@NotNull
	private String copyItem(@NotNull String api, @NotNull byte[] content) throws ErrorResponseException {
		checkExpired();

		SyncResponse response = requestTool.postMetadata(api, content);

		// if not 202 Accepted raise ErrorResponseException
		requestTool.errorHandling(response, HttpURLConnection.HTTP_ACCEPTED);

		return response.getHeader().get("Location").get(0);
	}




	/*
	*************************************************************
	*
	* Moving OneDrive Item
	*
	*************************************************************
	 */

	@NotNull
	public BaseItem moveItem(@NotNull String srcId, @NotNull String destId) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":{\"id\":\"" + destId + "\"}}").getBytes();
		return moveItem(ITEM_ID_PREFIX + srcId, content);
	}

	@NotNull
	public BaseItem moveItem(@NotNull String srcId, @NotNull PathPointer destPath) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":" + destPath.toJson() + "}").getBytes();
		return moveItem(ITEM_ID_PREFIX + srcId, content);
	}

	@NotNull
	public BaseItem moveItem(@NotNull PathPointer srcPath, @NotNull String destId) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":{\"id\":\"" + destId + "\"}}").getBytes();
		return moveItem(srcPath.toASCIIApi(), content);
	}

	@NotNull
	public BaseItem moveItem(@NotNull BasePointer src, @NotNull BasePointer dest) throws ErrorResponseException {
		byte[] content = ("{\"parentReference\":" + dest.toJson() + "}").getBytes();
		return moveItem(src.toASCIIApi(), content);
	}

	@NotNull
	private BaseItem moveItem(@NotNull String api, @NotNull byte[] content) throws ErrorResponseException {
		checkExpired();

		final CountDownLatch latch = new CountDownLatch(1);
		final BaseItem[] newItem = new BaseItem[1];
		ResponseFuture responseFuture =
				requestTool.patchMetadataAsync(api, content,
						new ResponseFutureListener() {
							@Override public void operationComplete(ResponseFuture future) throws Exception {
								newItem[0] = requestTool.parseAndHandle(
										future.response(),
										future.get(),
										HttpURLConnection.HTTP_OK,
										BaseItem.class);
								latch.countDown();
							}
						});

		responseFuture.syncUninterruptibly();
		try {
			latch.await();
		}
		catch (InterruptedException e) {
			throw new InternalException("Exception occurs while waiting lock in BaseItem#update()", e);
		}

		return newItem[0];
	}




	/*
	*************************************************************
	*
	* Creating folder
	*
	*************************************************************
	 */


	/**
	 * Implementation of <a href='https://dev.onedrive.com/items/create.htm'>detail</a>.
	 * <p>
	 * {@// TODO: Enhance javadoc }
	 * {@// TODO: Implement '@name.conflictBehavior' }
	 *
	 * @param parentId Parent ID that creating folder inside.
	 * @param name     New folder name.
	 * @return New folder's object.
	 * @throws RuntimeException If creating folder or converting response is fails.
	 */
	@NotNull
	public FolderItem createFolder(@NotNull String parentId, @NotNull String name) throws ErrorResponseException {
		byte[] content = ("{\"name\":\"" + name + "\",\"folder\":{}}").getBytes();
		return createFolder("/drive/items/" + parentId + "/children", content);
	}

	/**
	 * Implementation of <a href='https://dev.onedrive.com/items/create.htm'>detail</a>.
	 * <p>
	 * {@// TODO: Enhance javadoc }
	 * {@// TODO: Implement '@name.conflictBehavior' }
	 *
	 * @param parent Parent pointer that creating folder inside. (either ID or path)
	 * @param name   New folder name.
	 * @return New folder's object.
	 * @throws RuntimeException If creating folder or converting response is fails.
	 */
	@NotNull
	public FolderItem createFolder(@NotNull BasePointer parent, @NotNull String name) throws ErrorResponseException {
		byte[] content = ("{\"name\":\"" + name + "\",\"folder\":{}}").getBytes();
		return createFolder(parent.resolveOperator(Operator.CHILDREN), content);
	}

	@NotNull
	private FolderItem createFolder(@NotNull String api, @NotNull byte[] content) throws ErrorResponseException {
		checkExpired();

		SyncResponse response = requestTool.postMetadata(api, content);

		return requestTool.parseAndHandle(response, HttpURLConnection.HTTP_CREATED, FolderItem.class);
	}




	/*
	*************************************************************
	*
	* Downloading files
	*
	*************************************************************
	 */


	public void download(@NotNull String fileId, @NotNull Path parent) throws ErrorResponseException, IOException {
		ObjectNode jsonNodes = requestTool.doGetJson(ITEM_ID_PREFIX + fileId + "?select=name,file,folder");

		if (jsonNodes.has("folder"))
			throw new IllegalArgumentException("given " + fileId + " is ID of folder. only ID of file is accepted");

		_download(Client.ITEM_ID_PREFIX + fileId + "/content", parent, jsonNodes.get("name").asText());
	}

	public void download(@NotNull String fileId, @NotNull Path parent, @NotNull String fileName)
			throws IOException, ErrorResponseException {
		_download(Client.ITEM_ID_PREFIX + fileId + "/content", parent, fileName);
	}

	public void download(@NotNull BasePointer file, @NotNull Path parent) throws ErrorResponseException, IOException {
		ObjectNode jsonNodes = requestTool.doGetJson(file.toASCIIApi() + "?select=name,file,folder");

		if (jsonNodes.has("folder"))
			throw new IllegalArgumentException(
					"given " + file.toString() + " is pointer of folder. only pointer of file is accepted");

		_download(file.resolveOperator(Operator.CONTENT), parent, jsonNodes.get("name").asText());
	}

	public void download(@NotNull BasePointer file, @NotNull Path parent, @NotNull String fileName)
			throws ErrorResponseException, IOException {
		_download(file.resolveOperator(Operator.CONTENT), parent, fileName);
	}

	private void _download(@NotNull String api, @NotNull Path parent, @NotNull String fileName)
			throws IOException, ErrorResponseException {
		Path parentBackup = parent.toAbsolutePath();
		parent = parentBackup.resolve(fileName);

		// it's illegal if and only if `parent` exists but not directory.
		if (Files.exists(parentBackup) && !Files.isDirectory(parentBackup))
			throw new IllegalArgumentException(parentBackup + " already exists and isn't folder.");

		Files.createDirectories(parentBackup);

		SyncResponse response = requestTool.newRequest(api).doGet();

		requestTool.errorHandling(response, HttpURLConnection.HTTP_OK);

		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
				parent,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);

		ByteBuffer contentBuf = ByteBuffer.wrap(response.getContent(), 0, response.getLength());

		fileChannel.write(contentBuf, 0);
	}


	public DownloadFuture downloadAsync(@NotNull String fileId, @NotNull Path downloadFolder, @NotNull String fileName)
			throws IOException {
		Path parentBackup = downloadFolder.toAbsolutePath();
		downloadFolder = parentBackup.resolve(fileName);

		// it's illegal if and only if `downloadFolder` exists but not directory.
		if (Files.exists(parentBackup) && !Files.isDirectory(parentBackup))
			throw new IllegalArgumentException(parentBackup + " already exists and isn't folder.");

		Files.createDirectories(parentBackup);

		String fullUrl = RequestTool.BASE_URL + Client.ITEM_ID_PREFIX + fileId + "/content";
		try {
			return new AsyncDownloadClient(
					RequestTool.getGroup(),
					new URI(fullUrl),
					downloadFolder,
					getFullToken(),
					requestTool).execute();
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Wrong fileId (" + fileId + "), full URL : \"" + fullUrl + "\".", e);
		}
	}

	public DownloadFuture downloadAsync(@NotNull BasePointer pointer,
										@NotNull Path downloadFolder, @NotNull String fileName) throws IOException {

		Path parentBackup = downloadFolder.toAbsolutePath();
		downloadFolder = parentBackup.resolve(fileName);

		// it's illegal if and only if `downloadFolder` exists but not directory.
		if (Files.exists(parentBackup) && !Files.isDirectory(parentBackup))
			throw new IllegalArgumentException(parentBackup + " already exists and isn't folder.");

		Files.createDirectories(parentBackup);

		String fullUrl = RequestTool.BASE_URL + pointer.resolveOperator(Operator.CONTENT);
		try {
			return new AsyncDownloadClient(
					RequestTool.getGroup(),
					new URI(fullUrl),
					downloadFolder,
					getFullToken(),
					requestTool).execute();
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Wrong pointer (" + pointer + "), full URL : \"" + fullUrl + "\".", e);
		}
	}



	/*
	*************************************************************
	*
	* Deleting item
	*
	*************************************************************
	 */


	public void deleteItem(@NotNull String id) throws ErrorResponseException {
		SyncResponse response = requestTool.newRequest(Client.ITEM_ID_PREFIX + id).doDelete();

		// if response isn't 204 No Content
		requestTool.errorHandling(response, HttpURLConnection.HTTP_NO_CONTENT);
	}

	public void deleteItem(@NotNull BasePointer id) throws ErrorResponseException {
		SyncResponse response = requestTool.newRequest(id.toASCIIApi()).doDelete();

		// if response isn't 204 No Content
		requestTool.errorHandling(response, HttpURLConnection.HTTP_NO_CONTENT);
	}





	/*
	*************************************************************
	*
	* Custom Getter
	*
	*************************************************************
	 */


	public boolean isExpired() {
		return System.currentTimeMillis() - lastRefresh >= expirationTime;
	}

	public boolean isLogin() {
		return authCode != null && accessToken != null && userId != null && refreshToken != null;
	}

	public @NotNull String getTokenType() {
		checkExpired();
		//noinspection ConstantConditions
		return tokenType;
	}

	public @NotNull String getAccessToken() {
		checkExpired();
		//noinspection ConstantConditions
		return accessToken;
	}

	public @NotNull String getUserId() {
		checkExpired();
		//noinspection ConstantConditions
		return userId;
	}

	public @NotNull String getRefreshToken() {
		checkExpired();
		//noinspection ConstantConditions
		return refreshToken;
	}

	public @NotNull String getAuthCode() {
		checkExpired();
		//noinspection ConstantConditions
		return authCode;
	}

	public @NotNull String getFullToken() {
		checkExpired();
		//noinspection ConstantConditions
		return fullToken;
	}

	@NotNull
	@JsonIgnore
	public RequestTool requestTool() {
		return requestTool;
	}

	@NotNull
	@JsonIgnore
	public ObjectMapper mapper() {
		return mapper;
	}

	@JsonIgnoreType
	private static class IgnoreMe {
	}
}
