package org.onedrive.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.Client;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.exceptions.InternalException;
import org.onedrive.exceptions.InvalidJsonException;
import org.onedrive.network.*;
import org.onedrive.network.async.AsyncResponseHandler;
import org.onedrive.network.async.AsyncRequest;
import org.onedrive.network.async.AsyncRequestHandler;
import org.onedrive.network.sync.SyncRequest;
import org.onedrive.network.sync.SyncResponse;

import java.io.IOException;
import java.net.*;

import static com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_ROOT_VALUE;

/**
 * {@// TODO: Enhance javadoc}
 * {@// TODO: Support OneDrive for Business}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class OneDriveRequest {
	public static final String SCHEME = "https";
	public static final String HOST = "api.onedrive.com/v1.0";
	/**
	 * OneDrive API base URL.
	 */
	public static final String BASE_URL = SCHEME + "://" + HOST;
	@Getter protected final EventLoopGroup group;
	private final ObjectMapper mapper;
	@Getter private final Client client;


	public OneDriveRequest(@NotNull Client client, @NotNull ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
		group = new NioEventLoopGroup();
	}


	/**
	 * <h1>Refrain to use this method. you can find API that wants to process in {@link Client}.</h1>
	 * Make {@link SyncRequest} object with given {@code api} for programmer's convenience.<br>
	 * <br>
	 * {@code api} must fallow API form. Note that it must be encoded. otherwise this will not work properly.
	 * <br>
	 * Example:<br>
	 * {@code OneDriveRequest.newRequest("/drives")},
	 * {@code OneDriveRequest.newRequest("/drive/items/485BEF1A80539148!115")},
	 * {@code OneDriveRequest.newRequest("/drive/root:/Documents")}
	 *
	 * @param api API to request. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *            <tt>/drive/root:/{item-path}</tt>)
	 * @return {@link SyncRequest} object that linked to {@code api} with access token.
	 * @throws InternalException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                           <tt>"http"</tt> or <tt>"https"</tt>.
	 * @see OneDriveRequest#newRequest(String)
	 */
	@NotNull
	public SyncRequest newRequest(@NotNull String api) {
		try {
			URL requestUrl = new URL(BASE_URL + api);
			return newRequest(requestUrl);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Wrong URL form. Should check code's String: \"" + BASE_URL + api + "\"");
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
	 * String BASE = "https://api.onedrive.com/v1.0";
	 * {@code OneDriveRequest.newRequest(new URL(BASE + "/drives"))},
	 * {@code OneDriveRequest.newRequest(new URL(BASE + "/drive/items/485BEF1A80539148!115"))},
	 * {@code OneDriveRequest.newRequest(new URL(BASE + "/drive/root:/Documents"))}
	 *
	 * @param url full URL of API to request. Note that it must be encoded.
	 * @return {@link SyncRequest} object that linked to {@code url} with access token.
	 */
	@NotNull
	public SyncRequest newRequest(@NotNull URL url) {
		SyncRequest request = new SyncRequest(url);
		request.setHeader("Authorization", client.getFullToken());
		return request;
	}





	/* *******************************************
	 *
	 *           Non Blocking Member
	 *
	 *********************************************/


	@NotNull
	public AsyncRequestHandler doAsync(@NotNull HttpMethod method, @NotNull String api) {
		AsyncRequest asyncRequest;
		try {
			asyncRequest = new AsyncRequest(group, new URI(BASE_URL + api), method);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".");
		}
		asyncRequest.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		return asyncRequest.send();
	}

	@NotNull
	public AsyncRequestHandler doAsync(@NotNull HttpMethod method, @NotNull URI uri) {
		AsyncRequest asyncRequest = new AsyncRequest(group, uri, method);
		asyncRequest.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		return asyncRequest.send();
	}

	@NotNull
	public AsyncRequestHandler doAsync(@NotNull HttpMethod method, @NotNull String api,
									   @NotNull AsyncResponseHandler onComplete) {
		AsyncRequest asyncRequest;
		try {
			asyncRequest = new AsyncRequest(group, new URI(BASE_URL + api), method, onComplete);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".");
		}
		asyncRequest.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		return asyncRequest.send();
	}

	@NotNull
	public AsyncRequestHandler doAsync(@NotNull HttpMethod method, @NotNull URI uri,
									   @NotNull AsyncResponseHandler onComplete) {
		AsyncRequest asyncRequest = new AsyncRequest(group, uri, method, onComplete);
		asyncRequest.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		return asyncRequest.send();
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
	 * OneDriveRequest.doGetJson("/drives", "AAD....2XA")
	 * }
	 *
	 * @param api API to get. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *            <tt>/drive/root:/{item-path}</tt>)
	 * @return that parsed from HTTP GET's json response.
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see ObjectNode
	 */
	public ObjectNode doGetJson(@NotNull String api) throws ErrorResponseException {
		SyncResponse response = newRequest(api).doGet();

		try {
			if (response.getCode() == HttpURLConnection.HTTP_OK) {
				return (ObjectNode) mapper.readTree(response.getContent());
			}
			else {
				ErrorResponse error = mapper.readValue(response.getContent(), ErrorResponse.class);
				throw new ErrorResponseException(HttpURLConnection.HTTP_OK, response.getCode(),
						error.getCode(), error.getMessage());
			}
		}
		catch (JsonProcessingException e) {
			throw new InvalidJsonException(e, response.getCode(), response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			// TODO: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public SyncResponse postMetadata(@NotNull String api, byte[] content) {
		SyncRequest request = newRequest(api);
		request.setHeader("Content-Type", "application/json");
		request.setHeader("Prefer", "respond-async");
		return request.doPost(content);
	}

	@NotNull
	public AsyncRequestHandler patchMetadata(@NotNull String api, byte[] content) {
		AsyncRequestHandler clientHandler = patchMetadataAsync(api, content, null);

		try {
			clientHandler.getBlockingCloseFuture().sync();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("DEV: Error while waiting patch done.");
		}

		return clientHandler;
	}

	@NotNull
	public AsyncRequestHandler patchMetadataAsync(@NotNull String api, byte[] content) {
		return patchMetadataAsync(api, content, null);
	}

	@NotNull
	public AsyncRequestHandler patchMetadataAsync(@NotNull String api, byte[] content,
												  @Nullable AsyncResponseHandler handler) {
		AsyncRequest asyncRequest;
		try {
			asyncRequest = new AsyncRequest(group, new URI(BASE_URL + api), HttpMethod.PATCH, handler);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Wrong character in `api` at " + e.getIndex(), e);
		}

		asyncRequest.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		asyncRequest.setHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
		asyncRequest.setHeader(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(content.length));
		asyncRequest.setHeader("Prefer", "respond-async");

		return asyncRequest.send(content);
	}





	/* *******************************************
	 *
	 *                      Tools
	 *
	 *********************************************/


	public void errorHandling(@NotNull SyncResponse response, int expectedCode) throws ErrorResponseException {
		if (response.getCode() != expectedCode) {
			try {
				ErrorResponse error = mapper
						.readerFor(ErrorResponse.class)
						.with(UNWRAP_ROOT_VALUE)
						.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(),
						error.getCode(), error.getMessage());
			}
			catch (JsonProcessingException e) {
				throw new InvalidJsonException(e, response.getCode(), response.getContent());
			}
			catch (IOException e) {
				e.printStackTrace();
				// TODO: custom exception
				throw new RuntimeException("DEV: Unrecognizable json response.", e);
			}
		}
	}

	public void errorHandling(@NotNull HttpResponse response, @NotNull DirectByteInputStream inputStream,
							  int expectedCode)
			throws ErrorResponseException {
		if (response.status().code() != expectedCode) {
			try {
				ErrorResponse error = mapper
						.readerFor(ErrorResponse.class)
						.with(UNWRAP_ROOT_VALUE)
						.readValue(inputStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
			catch (JsonProcessingException e) {
				throw new InvalidJsonException(e, response.status().code(), inputStream.getRawBuffer());
			}
			catch (IOException e) {
				e.printStackTrace();
				// TODO: custom exception
				throw new RuntimeException("DEV: Unrecognizable json response.", e);
			}
		}
	}


	public <T> T parseAndHandle(@NotNull SyncResponse response, int expectedCode, Class<T> classType)
			throws ErrorResponseException {
		try {
			if (response.getCode() == expectedCode) {
				return mapper.readValue(response.getContent(), classType);
			}
			else {
				ErrorResponse error = mapper
						.readerFor(ErrorResponse.class)
						.with(UNWRAP_ROOT_VALUE)
						.readValue(response.getContent());
				throw new ErrorResponseException(expectedCode, response.getCode(),
						error.getCode(), error.getMessage());
			}
		}
		catch (JsonProcessingException e) {
			throw new InvalidJsonException(e, response.getCode(), response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			// TODO: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}

	public <T> T parseAndHandle(@NotNull HttpResponse response, @NotNull DirectByteInputStream inputStream,
								int expectedCode, Class<T> classType)
			throws ErrorResponseException {
		try {
			if (response.status().code() == expectedCode) {
				return mapper.readValue(inputStream, classType);
			}
			else {
				ErrorResponse error = mapper
						.readerFor(ErrorResponse.class)
						.with(UNWRAP_ROOT_VALUE)
						.readValue(inputStream);
				throw new ErrorResponseException(expectedCode, response.status().code(),
						error.getCode(), error.getMessage());
			}
		}
		catch (JsonProcessingException e) {
			throw new InvalidJsonException(e, response.status().code(), inputStream.getRawBuffer());
		}
		catch (IOException e) {
			e.printStackTrace();
			// TODO: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}
}
