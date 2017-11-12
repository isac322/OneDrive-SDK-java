package com.bhyoo.onedrive.client;

import com.bhyoo.onedrive.client.auth.AuthenticationInfo;
import com.bhyoo.onedrive.container.Drive;
import com.bhyoo.onedrive.container.ResponsePage;
import com.bhyoo.onedrive.container.items.*;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.ErrorResponse;
import com.bhyoo.onedrive.network.UploadSession;
import com.bhyoo.onedrive.network.async.*;
import com.bhyoo.onedrive.network.sync.SyncRequest;
import com.bhyoo.onedrive.network.sync.SyncResponse;
import com.bhyoo.onedrive.utils.ByteBufStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

// TODO: Enhance javadoc
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
	private static final EventLoopGroup group;
	private static final Class<? extends SocketChannel> socketChannelClass;

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
	}

	@Getter private final ObjectMapper mapper;
	private final ObjectReader errorReader;
	private final ObjectReader driveItemReader;
	private final ObjectReader driveItemPageReader;
	private final ObjectReader fileItemReader;
	private final ObjectReader folderItemReader;
	private final ObjectReader driveReader;
	private final ObjectReader drivePageReader;
	private final ObjectReader authReader;
	@Getter private final Client client;

	public RequestTool(@NotNull Client client, @NotNull ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
		this.errorReader = mapper.readerFor(ErrorResponse.class).with(UNWRAP_ROOT_VALUE);
		this.driveItemReader = mapper.readerFor(AbstractDriveItem.class);
		this.driveItemPageReader = mapper.readerFor(
				mapper.getTypeFactory().constructParametricType(
						ResponsePage.class, AbstractDriveItem.class));
		this.fileItemReader = mapper.readerFor(DefaultFileItem.class);
		this.folderItemReader = mapper.readerFor(DefaultFolderItem.class);
		this.driveReader = mapper.readerFor(Drive.class);
		this.drivePageReader =
				mapper.readerFor(mapper.getTypeFactory().constructParametricType(ResponsePage.class, Drive.class));
		this.authReader = mapper.readerFor(AuthenticationInfo.class);
	}

	public static EventLoopGroup group() {return group;}

	public static Class<? extends SocketChannel> socketChannelClass() {return socketChannelClass;}

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





	/* *******************************************
	 *
	 *           Non Blocking Member
	 *
	 *********************************************/


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
				.handler(new AsyncDefaultInitializer(new DriveItemHandler(promise, mapper)));


		bootstrap.connect(REAL_HOST, 443).addListener(new ChannelFutureListener() {
			@Override public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					future.channel().writeAndFlush(request);
				}
			}
		});

		return promise;
	}

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
				return driveItemReader.readValue(body);
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





	/* *******************************************
	 *
	 *             Blocking member
	 *
	 *********************************************/


	/**
	 * <a href='https://dev.onedrive.com/items/get.htm'>https://dev.onedrive.com/items/get.htm</a>
	 * <br><br>
	 * Instantly send GET request to OneDrive with {@code api}, and return parsed JSON response object.
	 * <br><br>
	 * It is assured that the return value is always not {@code null}, if the response is successfully received.
	 * (when 200 OK or even non-OK response like 404 NOT FOUND or something is received).
	 * <br>
	 * But if other error happens while requesting (for example network error, bad api, wrong token... etc.),
	 * it will throw {@link RuntimeException}.
	 * <br><br>
	 * {@code api} must fallow API form.
	 * <br>
	 * Example:<br>
	 * {@code
	 * RequestTool.doGetJson("/drives", "AAD....2XA")
	 * }
	 *
	 * @param api API to get. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *            <tt>/drive/root:/{item-path}</tt>)
	 *
	 * @return that parsed from HTTP GET's json response.
	 *
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see ObjectNode
	 */
	public ObjectNode doGetJson(@NotNull String api) throws ErrorResponseException {
		SyncResponse response = newRequest(api).doGet();
		byte[] content = response.getContent();

		try {
			if (response.getCode() == HTTP_OK) {
				return (ObjectNode) mapper.readTree(content);
			}
			else {
				ErrorResponse error = errorReader.readValue(content);
				throw new ErrorResponseException(HTTP_OK, response.getCode(), error.getCode(), error.getMessage());
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
					HttpResponse response = future.response();
					if (response.status().code() == HTTP_OK) {
						UploadSession session = parseAndHandle(response, future.get(), HTTP_OK, UploadSession.class);
						uploadPromise.setUploadURI(new URI(session.getUploadUrl()));

						AsyncUploadClient uploadClient = new AsyncUploadClient(group, uploadPromise);
						uploadClient.execute();
					}
					else errorHandling(response, future.get(), HTTP_OK);
				}
			}
		});

		return uploadPromise;
	}




	/* *******************************************
	 *
	 *                      Tools
	 *
	 *********************************************/


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

	public DriveItem parseBaseItemAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, driveItemReader);
	}

	public ResponsePage<DriveItem> parseBaseItemPageAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, driveItemPageReader);
	}

	public FileItem parseFileItemAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, fileItemReader);
	}

	public FolderItem parseFolderItemAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, folderItemReader);
	}

	public Drive parseDriveAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, driveReader);
	}

	public ResponsePage<Drive> parseDrivePageAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, drivePageReader);
	}

	public AuthenticationInfo parseAuthAndHandle(@NotNull SyncResponse response, int expectedCode)
			throws ErrorResponseException {
		return parseAndHandle(response, expectedCode, authReader);
	}

	private <T> T parseAndHandle(@NotNull SyncResponse response, int expectedCode, ObjectReader reader)
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

	public <T> T parseAndHandle(@NotNull HttpResponse response, @NotNull ByteBufStream byteBufStream,
								int expectedCode, Class<T> classType) throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				return mapper.readValue(byteBufStream, classType);
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
	}
}
