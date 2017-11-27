package com.bhyoo.onedrive.client;

import com.bhyoo.onedrive.client.auth.AuthenticationInfo;
import com.bhyoo.onedrive.container.items.*;
import com.bhyoo.onedrive.container.pager.DriveItemPager;
import com.bhyoo.onedrive.container.pager.DriveItemPager.DriveItemPage;
import com.bhyoo.onedrive.container.pager.DrivePager;
import com.bhyoo.onedrive.container.pager.DrivePager.DrivePage;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.ErrorResponse;
import com.bhyoo.onedrive.network.UploadSession;
import com.bhyoo.onedrive.network.async.*;
import com.bhyoo.onedrive.network.sync.SyncRequest;
import com.bhyoo.onedrive.network.sync.SyncResponse;
import com.bhyoo.onedrive.utils.ByteBufStream;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_ROOT_VALUE;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.GZIP;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.net.HttpURLConnection.HTTP_OK;

// TODO: Support OneDrive for Business

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class RequestTool {
	public static final String SCHEME = "https";
	public static final String REAL_HOST = "graph.microsoft.com";
	public static final String HOST = REAL_HOST + "/v1.0";
	/**
	 * OneDrive API base URL.
	 */
	public static final String BASE_URL = SCHEME + "://" + HOST;
	public static final JsonFactory jsonFactory;
	private static final EventLoopGroup group;
	private static final Class<? extends SocketChannel> socketChannelClass;
	@Getter private static final ObjectMapper mapper;
	private static final ObjectReader errorReader;
	private static final ObjectReader authReader;
	private static final ObjectReader uploadSessionReader;

	static {
		EventLoopGroup tmpGroup;
		Class<? extends SocketChannel> tmpClass;
		try {
			tmpGroup = new EpollEventLoopGroup(4);
			tmpClass = EpollSocketChannel.class;
		}
		catch (Exception e) {
			tmpGroup = new NioEventLoopGroup(4);
			tmpClass = NioSocketChannel.class;
		}

		group = tmpGroup;
		socketChannelClass = tmpClass;


		mapper = new ObjectMapper();
		mapper.registerModule(new AfterburnerModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// in serialization, ignore null values.
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		errorReader = mapper.readerFor(ErrorResponse.class).with(UNWRAP_ROOT_VALUE);
		authReader = mapper.readerFor(AuthenticationInfo.class);
		uploadSessionReader = mapper.readerFor(UploadSession.class);

		jsonFactory = mapper.getFactory();
	}

	@Getter private final Client client;
	private final InjectableValues.Std clientInjectValue;


	public RequestTool(final @NotNull Client client) {
		this.client = client;

		clientInjectValue = new InjectableValues.Std().addValue("OneDriveClient", client);
	}


	public static EventLoopGroup group() {return group;}

	public static Class<? extends SocketChannel> socketChannelClass() {return socketChannelClass;}

	private static <T> T parseAndHandle(@NotNull SyncResponse response, int expectedCode, ObjectReader reader)
			throws ErrorResponseException {
		byte[] content = response.getContent();

		try {
			if (response.getCode() == expectedCode) {
				return reader.readValue(content);
			}
			else {
				ErrorResponse err = errorReader.readValue(content);
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (JsonProcessingException e) {
			throw new InvalidJsonException(e, response.getCode(), content);
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	private static @NotNull AbstractDriveItem parseDriveItem(@NotNull SyncResponse response, int expectedCode,
															 @NotNull Client client) throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				return AbstractDriveItem.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = errorReader.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	private static <T> T parseAndHandle(@NotNull HttpResponse response, @NotNull ByteBufStream byteBufStream,
										int expectedCode, ObjectReader reader) throws ErrorResponseException {
		byteBufStream.getRawBuffer().retain();

		try {
			if (response.status().code() == expectedCode) {
				return reader.readValue(byteBufStream);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (JsonProcessingException e) {
			throw new InvalidJsonException(e, response.status().code(), byteBufStream.getRawBuffer());
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
		finally {
			byteBufStream.getRawBuffer().release();
		}
	}

	private static @NotNull AbstractDriveItem parseDriveItem(@NotNull HttpResponse response,
															 @NotNull ByteBufStream byteBufStream,
															 int expectedCode,
															 @NotNull Client client) throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				return AbstractDriveItem.deserialize(client, parser, true);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}


	/**
	 * <h1>Refrain to use this method. you can find API that wants to process in {@link Client}.</h1>
	 * Make {@link SyncRequest} object with given {@code api} for programmer's convenience.<br>
	 * <br>
	 * {@code api} must fallow API form. Note that it must be encoded. otherwise this will not work properly.
	 * <br>
	 * Example:<br>
	 * {@code RequestTool.newRequest("/drives")},
	 * {@code RequestTool.newRequest("/drive/items/485BEF1A80539148!115")},
	 * {@code RequestTool.newRequest("/drive/root:/Documents")}
	 *
	 * @param api API to request. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *            <tt>/drive/root:/{item-path}</tt>)
	 *
	 * @return {@link SyncRequest} object that linked to {@code api} with access token.
	 *
	 * @throws InternalException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                           <tt>"http"</tt> or <tt>"https"</tt>.
	 * @see RequestTool#newRequest(String)
	 */
	@NotNull
	public SyncRequest newRequest(@NotNull String api) {
		try {
			return new SyncRequest(new URL(BASE_URL + api))
					.setHeader(AUTHORIZATION, client.getFullToken())
					// TODO: GZIP .setHeader(ACCEPT_ENCODING, GZIP)
					.setHeader(ACCEPT, APPLICATION_JSON);
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(
					"Wrong URL form. Should check code's String: \"" + BASE_URL + api + "\"", e);
		}
	}





	/* *******************************************
	 *
	 *           Non Blocking Member
	 *
	 *********************************************/

	/**
	 * <h1>Refrain to use this method. you can find API that wants to process in {@link Client}.</h1>
	 * Make {@link SyncRequest} object with given {@code url} for programmer's convenience.<br>
	 * <br>
	 * {@code url} must fallow API form and contain full URL. Note that it must be encoded. otherwise this will not
	 * work properly.
	 * <br>
	 * Example:<br>
	 * String BASE = "https://graph.microsoft.com/v1.0";
	 * {@code RequestTool.newRequest(new URL(BASE + "/drives"))},
	 * {@code RequestTool.newRequest(new URL(BASE + "/drive/items/485BEF1A80539148!115"))},
	 * {@code RequestTool.newRequest(new URL(BASE + "/drive/root:/Documents"))}
	 *
	 * @param url full URL of API to request. Note that it must be encoded.
	 *
	 * @return {@link SyncRequest} object that linked to {@code url} with access token.
	 */
	@NotNull
	public SyncRequest newRequest(@NotNull URL url) {
		return new SyncRequest(url)
				.setHeader(AUTHORIZATION, client.getFullToken())
				// TODO: GZIP .setHeader(ACCEPT_ENCODING, GZIP)
				.setHeader(ACCEPT, APPLICATION_JSON);
	}

	public ResponseFuture doAsync(@NotNull HttpMethod method, @NotNull String api) {
		try {
			return doAsync(method, new URI(BASE_URL + api));
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".", e);
		}
	}

	public ResponseFuture doAsync(@NotNull HttpMethod method, @NotNull URI uri) {
		return new AsyncClient(group, method, uri)
				.setHeader(AUTHORIZATION, client.getFullToken())
				.execute();
	}

	public ResponseFuture doAsync(@NotNull HttpMethod method, @NotNull String api,
								  @NotNull ResponseFutureListener onComplete) {
		try {
			return doAsync(method, new URI(BASE_URL + api), onComplete);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".", e);
		}
	}

	public ResponseFuture doAsync(@NotNull HttpMethod method, @NotNull URI uri,
								  @NotNull ResponseFutureListener onComplete) {
		return new AsyncClient(group, method, uri)
				.setHeader(AUTHORIZATION, client.getFullToken())
				.execute()
				.addListener(onComplete);
	}

	public DriveItemFuture getItemAsync(@NotNull String asciiApi) {
		final DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, BASE_URL + asciiApi);
		request.headers()
				.set(HttpHeaderNames.HOST, REAL_HOST)
				.set(ACCEPT_ENCODING, GZIP)
				.set(AUTHORIZATION, client.getFullToken());

		DefaultDriveItemPromise promise = new DefaultDriveItemPromise(group.next());


		// Configure the client.
		Bootstrap bootstrap = new Bootstrap()
				.group(group)
				.channel(socketChannelClass())
				.handler(new AsyncDefaultInitializer(new DriveItemHandler(promise, this)));


		bootstrap.connect(REAL_HOST, 443).addListener(new ChannelFutureListener() {
			@Override public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					future.channel().writeAndFlush(request);
				}
			}
		});

		return promise;
	}





	/* *******************************************
	 *
	 *             Blocking member
	 *
	 *********************************************/

	public DriveItem getItem(@NotNull String asciiApi) throws ErrorResponseException {
		HttpsURLConnection httpsConnection;

		try {
			httpsConnection = (HttpsURLConnection) new URL(BASE_URL + asciiApi).openConnection();
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException(e);
		}

		httpsConnection.setRequestProperty(AUTHORIZATION.toString(), client.getFullToken());
		httpsConnection.setRequestProperty(ACCEPT.toString(), APPLICATION_JSON.toString());
		// TODO: GZIP httpsConnection.setRequestProperty(ACCEPT_ENCODING.toString(), GZIP.toString());

		try {
			int code = httpsConnection.getResponseCode();
			InputStream body;

			if (code == HTTP_OK) {
				body = httpsConnection.getInputStream();
				JsonParser parser = jsonFactory.createParser(body);
				parser.nextToken();
				return AbstractDriveItem.deserialize(client, parser, true);
			}
			else {
				body = httpsConnection.getErrorStream();
				ErrorResponse error = errorReader.readValue(body);
				throw new ErrorResponseException(HTTP_OK, code, error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
		finally {
			httpsConnection.disconnect();
		}
	}


	public SyncResponse postMetadata(@NotNull String api, byte[] content) {
		return newRequest(api)
				.setHeader(CONTENT_TYPE, APPLICATION_JSON)
				.setHeader("Prefer", "respond-async")
				.doPost(content);
	}

	public ResponseFuture patchMetadataAsync(@NotNull String api, byte[] content) {
		AsyncClient asyncClient;
		try {
			asyncClient = new AsyncClient(group, PATCH, new URI(BASE_URL + api), content);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Wrong character in `api` at " + e.getIndex(), e);
		}

		asyncClient.setHeader(AUTHORIZATION, client.getFullToken());
		asyncClient.setHeader(CONTENT_TYPE, APPLICATION_JSON);
		asyncClient.setHeader(CONTENT_LENGTH, String.valueOf(content.length));
		asyncClient.setHeader("Prefer", "respond-async");

		return asyncClient.execute();
	}

	public ResponseFuture patchMetadataAsync(@NotNull String api, byte[] content,
											 @NotNull ResponseFutureListener handler) {
		AsyncClient asyncClient;
		try {
			asyncClient = new AsyncClient(group, PATCH, new URI(BASE_URL + api), content);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Wrong character in `api` at " + e.getIndex(), e);
		}

		asyncClient.setHeader(AUTHORIZATION, client.getFullToken());
		asyncClient.setHeader(CONTENT_TYPE, APPLICATION_JSON);
		asyncClient.setHeader(CONTENT_LENGTH, String.valueOf(content.length));
		asyncClient.setHeader("Prefer", "respond-async");

		return asyncClient.execute().addListener(handler);
	}




	/* *******************************************
	 *
	 *                      Tools
	 *
	 *********************************************/

	public UploadFuture upload(@NotNull String api, @NotNull Path filePath) throws IOException {
		URI uri;
		try {
			uri = new URI(BASE_URL + api);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".", e);
		}

		if (!RequestTool.SCHEME.equalsIgnoreCase(uri.getScheme())) {
			throw new IllegalArgumentException("Wrong network scheme : \"" + uri.getScheme() + "\".");
		}

		final DefaultHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, uri.toASCIIString());

		request.headers()
				.set(HttpHeaderNames.HOST, uri.getHost())
				.set(ACCEPT_ENCODING, GZIP)
				.set(CONTENT_LENGTH, "0")
				.set(AUTHORIZATION, client.getFullToken());

		String host = uri.getHost();
		int port = 443;

		ResponsePromise responsePromise = new DefaultResponsePromise(group.next());

		// Configure the client.
		Bootstrap bootstrap = new Bootstrap()
				.group(group)
				.channel(socketChannelClass)
				.handler(new AsyncDefaultInitializer(new AsyncClientHandler(responsePromise)));


		bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
			@Override public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					future.channel().writeAndFlush(request);
				}
				else {
					future.channel().close();
				}
			}
		});

		final DefaultUploadPromise uploadPromise = new DefaultUploadPromise(group.next(), filePath);

		responsePromise.addListener(new ResponseFutureListener() {
			@Override public void operationComplete(ResponseFuture future) throws Exception {
				if (future.isSuccess()) {
					UploadSession session = parseUploadSessionAndHandle(future.response(), future.get(), HTTP_OK);
					uploadPromise.setUploadURI(new URI(session.getUploadUrl()));

					AsyncUploadClient uploadClient = new AsyncUploadClient(group, uploadPromise);
					uploadClient.execute();
				}
			}
		});

		return uploadPromise;
	}

	public void errorHandling(@NotNull SyncResponse response, int expectedCode) throws ErrorResponseException {
		if (response.getCode() != expectedCode) {
			byte[] content = response.getContent();

			try {
				ErrorResponse error = errorReader.readValue(content);
				throw new ErrorResponseException(expectedCode, response.getCode(),
						error.getCode(), error.getMessage());
			}
			catch (JsonProcessingException e) {
				throw new InvalidJsonException(e, response.getCode(), content);
			}
			catch (IOException e) {
				// FIXME: custom exception
				throw new RuntimeException("DEV: Unrecognizable json response.", e);
			}
		}
	}

	public void errorHandling(@NotNull HttpResponse response, @NotNull ByteBufStream byteBufStream,
							  int expectedCode) throws ErrorResponseException {
		if (response.status().code() != expectedCode) {
			try {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
			catch (JsonProcessingException e) {
				throw new InvalidJsonException(e, response.status().code(), byteBufStream.getRawBuffer());
			}
			catch (IOException e) {
				// FIXME: custom exception
				throw new RuntimeException("DEV: Unrecognizable json response.", e);
			}
		}
	}

	public @NotNull DriveItem parseDriveItemAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseDriveItem(response, expectedCode, client);
	}

	public @NotNull FileItem parseFileItemAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return (FileItem) parseDriveItem(response, expectedCode, client);
	}

	public @NotNull FolderItem parseFolderItemAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return (FolderItem) parseDriveItem(response, expectedCode, client);
	}

	public @NotNull DriveItemPage parseDriveItemPageAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				return DriveItemPage.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = errorReader.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DriveItemPager parseDriveItemPagerAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				return DriveItemPager.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = errorReader.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	/*
	Parse Drive
	 */

	public @NotNull Drive parseDriveAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				return Drive.deserialize(client, parser);
			}
			else {
				ErrorResponse err = errorReader.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DrivePage parseDrivePageAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				return DrivePage.deserialize(client, parser);
			}
			else {
				ErrorResponse err = errorReader.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DrivePager parseDrivePagerAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				return DrivePager.deserialize(client, parser);
			}
			else {
				ErrorResponse err = errorReader.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull AuthenticationInfo parseAuthAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, authReader.with(clientInjectValue));
	}


	public @NotNull DriveItem parseDriveItemAndHandle(@NotNull HttpResponse response,
													  @NotNull ByteBufStream byteBufStream,
													  int expectedCode) throws ErrorResponseException {
		return parseDriveItem(response, byteBufStream, expectedCode, client);
	}

	public @NotNull DriveItemPage parseDriveItemPageAndHandle(@NotNull HttpResponse response,
															  @NotNull ByteBufStream byteBufStream, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				return DriveItemPage.deserialize(client, parser, true);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DriveItemPager parseDriveItemPagerAndHandle(@NotNull HttpResponse response,
																@NotNull ByteBufStream byteBufStream, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				return DriveItemPager.deserialize(client, parser, true);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DriveItem[] parseDriveItemRecursiveAndHandle(@NotNull HttpResponse response,
																 @NotNull ByteBufStream byteBufStream,
																 int expectedCode) throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				return DriveItemPager.deserializeRecursive(client, parser, true);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DrivePage parseDrivePageAndHandle(@NotNull HttpResponse response,
													  @NotNull ByteBufStream byteBufStream, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				return DrivePage.deserialize(client, parser);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull DrivePager parseDrivePagerAndHandle(@NotNull HttpResponse response,
														@NotNull ByteBufStream byteBufStream, int expectedCode)
			throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				return DrivePager.deserialize(client, parser);
			}
			else {
				ErrorResponse error = errorReader.readValue(byteBufStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public @NotNull UploadSession parseUploadSessionAndHandle(@NotNull HttpResponse response,
															  @NotNull ByteBufStream byteBufStream,
															  int expectedCode) throws ErrorResponseException {
		return parseAndHandle(response, byteBufStream, expectedCode, uploadSessionReader);
	}
}
