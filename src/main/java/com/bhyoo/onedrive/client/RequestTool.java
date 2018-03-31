package com.bhyoo.onedrive.client;

import com.bhyoo.onedrive.client.auth.AuthenticationInfo;
import com.bhyoo.onedrive.container.items.*;
import com.bhyoo.onedrive.container.pager.DriveItemPager;
import com.bhyoo.onedrive.container.pager.DriveItemPager.DriveItemPage;
import com.bhyoo.onedrive.container.pager.DrivePager;
import com.bhyoo.onedrive.container.pager.DrivePager.DrivePage;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.network.ErrorResponse;
import com.bhyoo.onedrive.network.UploadSession;
import com.bhyoo.onedrive.network.async.*;
import com.bhyoo.onedrive.network.sync.SyncRequest;
import com.bhyoo.onedrive.network.sync.SyncResponse;
import com.bhyoo.onedrive.utils.ByteBufStream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
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
import java.util.zip.GZIPInputStream;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.GZIP;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.net.HttpURLConnection.HTTP_OK;


/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@SuppressWarnings("unchecked")
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

	static {
		EventLoopGroup tmpGroup;
		Class<? extends SocketChannel> tmpClass;
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			Class<?> loadClass = classLoader.loadClass("io.netty.channel.epoll.EpollEventLoopGroup");
			tmpGroup = (EventLoopGroup) loadClass.getConstructor(int.class).newInstance(4);
			tmpClass = (Class<? extends SocketChannel>)
					classLoader.loadClass("io.netty.channel.epoll.EpollSocketChannel");
		}
		catch (Exception e) {
			e.printStackTrace();
			tmpGroup = new NioEventLoopGroup(4);
			tmpClass = NioSocketChannel.class;
		}

		group = tmpGroup;
		socketChannelClass = tmpClass;


		jsonFactory = new JsonFactory();
	}

	@Getter private final Client client;


	public RequestTool(final @NotNull Client client) {
		this.client = client;
	}


	public static EventLoopGroup group() {return group;}

	public static Class<? extends SocketChannel> socketChannelClass() {return socketChannelClass;}

	public static URI api2Uri(@NotNull String api) {
		try {
			return new URI(BASE_URL + api);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".", e);
		}
	}


	private static @NotNull AbstractDriveItem parseDriveItem(@NotNull SyncResponse response, int expectedCode,
															 @NotNull Client client) throws ErrorResponseException {
		try {
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return AbstractDriveItem.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	private static @NotNull AbstractDriveItem parseDriveItem(@NotNull HttpResponse response,
															 @NotNull ByteBufStream byteBufStream,
															 int expectedCode,
															 @NotNull Client client) throws ErrorResponseException {
		try {
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return AbstractDriveItem.deserialize(client, parser, true);
			}
			else {
				ErrorResponse error = ErrorResponse.deserialize(parser, true);
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
	 * @param api API to request. It must starts with <code>/</code>, kind of API form. (like <code>/drives</code> or
	 *            <code>/drive/root:/{item-path}</code>)
	 *
	 * @return {@link SyncRequest} object that linked to {@code api} with access token.
	 *
	 * @throws InternalException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                           <code>"http"</code> or <code>"https"</code>.
	 * @see RequestTool#newRequest(String)
	 */
	@NotNull
	public SyncRequest newRequest(@NotNull String api) {
		try {
			return new SyncRequest(new URL(BASE_URL + api))
					.setHeader(AUTHORIZATION, client.getFullToken())
					.setHeader(ACCEPT_ENCODING, GZIP)
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
				.setHeader(ACCEPT_ENCODING, GZIP)
				.setHeader(ACCEPT, APPLICATION_JSON);
	}





	/* *******************************************
	 *
	 *           Non Blocking Member
	 *
	 *********************************************/

	public ResponseFuture doAsync(@NotNull HttpMethod method, @NotNull String api) {
		return doAsync(method, api2Uri(api));
	}

	public ResponseFuture doAsync(@NotNull HttpMethod method, @NotNull URI uri) {
		return new AsyncClient(group, method, uri)
				.setHeader(AUTHORIZATION, client.getFullToken())
				.execute();
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
			@Override public void operationComplete(ChannelFuture future) {
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
		httpsConnection.setRequestProperty(ACCEPT_ENCODING.toString(), GZIP.toString());

		try {
			int code = httpsConnection.getResponseCode();
			InputStream body;

			if (code == HTTP_OK) {
				if ("gzip".equals(httpsConnection.getContentEncoding()))
					body = new GZIPInputStream(httpsConnection.getInputStream());
				else
					body = httpsConnection.getInputStream();

				JsonParser parser = jsonFactory.createParser(body);
				parser.nextToken();
				return AbstractDriveItem.deserialize(client, parser, true);
			}
			else {
				body = httpsConnection.getErrorStream();
				JsonParser parser = jsonFactory.createParser(body);
				parser.nextToken();

				ErrorResponse error = ErrorResponse.deserialize(parser, true);
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
		AsyncClient asyncClient = new AsyncClient(group, PATCH, api2Uri(api), content);

		asyncClient.setHeader(AUTHORIZATION, client.getFullToken());
		asyncClient.setHeader(CONTENT_TYPE, APPLICATION_JSON);
		asyncClient.setHeader(CONTENT_LENGTH, String.valueOf(content.length));
		asyncClient.setHeader("Prefer", "respond-async");

		return asyncClient.execute();
	}

	public ResponseFuture patchMetadataAsync(@NotNull String api, byte[] content,
											 @NotNull ResponseFutureListener handler) {
		AsyncClient asyncClient = new AsyncClient(group, PATCH, api2Uri(api), content);

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

	public UploadFuture upload(@NotNull String api, @NotNull Path filePath) {
		URI uri = api2Uri(api);

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
			@Override public void operationComplete(ChannelFuture future) {
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
					try {
						UploadSession session = parseUploadSessionAndHandle(future.response(), future.get(), HTTP_OK);
						uploadPromise.setUploadURI(new URI(session.getUploadUrl()));

						AsyncUploadClient uploadClient = new AsyncUploadClient(group, uploadPromise);
						uploadClient.execute();
					}
					catch (ErrorResponseException err) {
						uploadPromise.setFailure(err);
					}
				}
				else {
					uploadPromise.setFailure(future.cause());
				}
			}
		});

		return uploadPromise;
	}

	private void uploadSession(@NotNull String api, @NotNull Path filePath) {

	}

	public void errorHandling(@NotNull SyncResponse response, int expectedCode) throws ErrorResponseException {
		if (response.getCode() != expectedCode) {
			try {
				JsonParser parser = jsonFactory.createParser(response.getContent());
				parser.nextToken();
				ErrorResponse error = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.getCode(),
						error.getCode(), error.getMessage());
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
				JsonParser parser = jsonFactory.createParser(byteBufStream);
				parser.nextToken();
				ErrorResponse error = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
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
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return DriveItemPage.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
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
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return DriveItemPager.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
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
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return Drive.deserialize(client, parser);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
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
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return DrivePage.deserialize(client, parser);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
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
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return DrivePager.deserialize(client, parser);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
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
		try {
			JsonParser parser = jsonFactory.createParser(response.getContent());
			parser.nextToken();

			if (response.getCode() == expectedCode) {
				return AuthenticationInfo.deserialize(parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.getCode(), err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
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
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return DriveItemPage.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						err.getCode(), err.getMessage());
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
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return DriveItemPager.deserialize(client, parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						err.getCode(), err.getMessage());
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
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return DriveItemPager.deserializeRecursive(client, parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						err.getCode(), err.getMessage());
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
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return DrivePage.deserialize(client, parser);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						err.getCode(), err.getMessage());
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
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return DrivePager.deserialize(client, parser);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						err.getCode(), err.getMessage());
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
		try {
			JsonParser parser = jsonFactory.createParser(byteBufStream);
			parser.nextToken();

			if (response.status().code() == expectedCode) {
				return UploadSession.deserialize(parser, true);
			}
			else {
				ErrorResponse err = ErrorResponse.deserialize(parser, true);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						err.getCode(), err.getMessage());
			}
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}
}
