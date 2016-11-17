package org.onedrive.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.onedrive.Client;
import org.onedrive.network.AsyncHttpsResponseHandler;
import org.onedrive.network.HttpsClient;
import org.onedrive.network.HttpsClientHandler;
import org.onedrive.network.legacy.HttpsRequest;
import org.onedrive.network.legacy.HttpsResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * {@// TODO: Enhance javadoc}
 * {@// TODO: Support OneDrive for Business}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class OneDriveRequest {
	/**
	 * OneDrive API base URL.
	 */
	@Getter private static final String BASE_URL = "https://api.onedrive.com/v1.0";
	@Getter protected final EventLoopGroup group;
	private final ObjectMapper mapper;
	@Getter private final Client client;


	public OneDriveRequest(@NotNull Client client, @NotNull ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
		group = new NioEventLoopGroup();
	}

	/* *******************************************
	 *
	 *             Blocking Static
	 *
	 *********************************************/


	/**
	 * <a href='https://dev.onedrive.com/items/get.htm'>https://dev.onedrive.com/items/get.htm</a>
	 * <br><br>
	 * Instantly send GET request to OneDrive with {@code api}, and return response.
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
	 * OneDriveRequest.doGet("/drives", "AAD....2XA")
	 * }
	 *
	 * @param api             API to get. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *                        <tt>/drive/root:/{item-path}</tt>)
	 * @param fullAccessToken OneDrive access token.
	 * @return HTTP GET's response object.
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see HttpsRequest#doGet()
	 */
	@NotNull
	public static HttpsResponse doGet(@NotNull String api, @NotNull String fullAccessToken) {
		return newOneDriveRequest(api, fullAccessToken).doGet();
	}

	/**
	 * <a href='https://dev.onedrive.com/items/get.htm'>https://dev.onedrive.com/items/get.htm</a>
	 * <br><br>
	 * Instantly send GET request {@code url}, and return response.
	 * <br><br>
	 * It is assured that the return value is always not {@code null}, if the response is successfully received.
	 * (when 200 OK or even non-OK response like 404 NOT FOUND or something is received).
	 * <br>
	 * But if other error happens while requesting (for example network error, bad url, wrong token... etc.),
	 * it will throw {@link RuntimeException}.
	 * <br><br>
	 * {@code url} must contains full URL.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.doGet(new URL("https://api.onedrive.com/v1.0/drive"), "AAD....2XA")
	 * }
	 *
	 * @param url             URL to get. It must contains full URL
	 *                        (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param fullAccessToken OneDrive access token.
	 * @return HTTP GET's response object.
	 * @throws RuntimeException If {@code url} form is incorrect or connection fails.
	 * @see HttpsRequest#doGet()
	 */
	@NotNull
	public static HttpsResponse doGet(@NotNull URL url, @NotNull String fullAccessToken) {
		return newOneDriveRequest(url, fullAccessToken).doGet();
	}


	@NotNull
	public static HttpsResponse doPost(@NotNull String api, @NotNull String fullAccessToken, @NotNull byte[] content) {
		return newOneDriveRequest(api, fullAccessToken).doPost(content);
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull String api, @NotNull String fullAccessToken, @NotNull String content) {
		return newOneDriveRequest(api, fullAccessToken).doPost(content);
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull URL url, @NotNull String fullAccessToken, @NotNull byte[] content) {
		return newOneDriveRequest(url, fullAccessToken).doPost(content);
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull URL url, @NotNull String fullAccessToken, @NotNull String content) {
		return newOneDriveRequest(url, fullAccessToken).doPost(content);
	}


	@NotNull
	public static HttpsResponse doPatch(@NotNull String api, @NotNull String fullAccessToken,
										@NotNull byte[] content) {
		return newOneDriveRequest(api, fullAccessToken).doPatch(content);
	}

	@NotNull
	public static HttpsResponse doPatch(@NotNull String api, @NotNull String fullAccessToken,
										@NotNull String content) {
		return newOneDriveRequest(api, fullAccessToken).doPatch(content);
	}

	@NotNull
	public static HttpsResponse doPatch(@NotNull URL url, @NotNull String fullAccessToken, @NotNull byte[] content) {
		return newOneDriveRequest(url, fullAccessToken).doPatch(content);
	}

	@NotNull
	public static HttpsResponse doPatch(@NotNull URL url, @NotNull String fullAccessToken, @NotNull String content) {
		return newOneDriveRequest(url, fullAccessToken).doPatch(content);
	}


	/**
	 * <a href="https://dev.onedrive.com/items/delete.htm">https://dev.onedrive.com/items/delete.htm</a>
	 * <br><br>
	 * Instantly send DELETE request to OneDrive with {@code api}, and return response.
	 * <br><br>
	 * It is assured that the return value is always not {@code api}, if the response is successfully received.
	 * (when 200 OK or even non-OK response like 404 NOT FOUND or something is received).
	 * <br>
	 * But if other error happens while requesting (for example network error, bad api, wrong token... etc.),
	 * it will throw {@link RuntimeException}.
	 * <br><br>
	 * {@code api} must fallow API form.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.doDelete("/drive/items/{item-id}", "AAD....2XA")
	 * }
	 *
	 * @param api             API to delete. It must starts with <tt>/</tt>, kind of API form. (like
	 *                        <tt>/drives</tt> or
	 *                        <tt>/drive/root:/{item-path}</tt>)
	 * @param fullAccessToken OneDrive access token.
	 * @return HTTP DELETE's response object.
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see HttpsRequest#doDelete()
	 */
	@NotNull
	public static HttpsResponse doDelete(@NotNull String api, @NotNull String fullAccessToken) {
		return newOneDriveRequest(api, fullAccessToken).doDelete();
	}

	/**
	 * <a href="https://dev.onedrive.com/items/delete.htm">https://dev.onedrive.com/items/delete.htm</a>
	 * <br><br>
	 * Instantly send DELETE request {@code url}, and return response.
	 * <br><br>
	 * It is assured that the return value is always not {@code null}, if the response is successfully received.
	 * (when 200 OK or even non-OK response like 404 NOT FOUND or something is received).
	 * <br>
	 * But if other error happens while requesting (for example network error, bad url, wrong token... etc.),
	 * it will throw {@link RuntimeException}.
	 * <br><br>
	 * {@code url} must contains full URL.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.doDelete(new URL("https://api.onedrive.com/v1.0/drive/items/{item-id}"), "AAD....2XA")
	 * }
	 *
	 * @param url             URL to delete. It must contains full URL
	 *                        (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param fullAccessToken OneDrive access token.
	 * @return HTTP DELETE's response object.
	 * @throws RuntimeException If {@code url} form is incorrect or connection fails.
	 * @see HttpsRequest#doDelete()
	 */
	@NotNull
	public static HttpsResponse doDelete(@NotNull URL url, @NotNull String fullAccessToken) {
		return newOneDriveRequest(url, fullAccessToken).doDelete();
	}


	/**
	 * Make {@link HttpsRequest} object with given {@code api} and {@code fullAccessToken} for programmer's
	 * convenience.
	 * <br><br>
	 * {@code api} must fallow API form.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.newOneDriveRequest("/drives", "AAD....2XA")
	 * }
	 *
	 * @param api             API to request. It must starts with <tt>/</tt>, kind of API form. (like
	 *                        <tt>/drives</tt> or
	 *                        <tt>/drive/root:/{item-path}</tt>)
	 * @param fullAccessToken OneDrive access token.
	 * @return {@link HttpsRequest} object that contains {@code api} and {@code fullAccessToken}.
	 * @throws RuntimeException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                          <tt>"http"</tt> or <tt>"https"</tt>.
	 */
	@NotNull
	public static HttpsRequest newOneDriveRequest(@NotNull String api, @NotNull String fullAccessToken) {
		try {
			URL requestUrl = new URL(BASE_URL + api);
			return newOneDriveRequest(requestUrl, fullAccessToken);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Code error. Wrong URL form. Should check code's String: \"" + BASE_URL + api + "\"");
		}
	}


	/**
	 * Make {@link HttpsRequest} object with given {@code url} and {@code fullAccessToken} for programmer's
	 * convenience.
	 * <br><br>
	 * {@code url} must contains full URL.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.newOneDriveRequest(new URL("https://api.onedrive.com/v1.0/drive/items/{item-id}"), "AAD....2XA")
	 * }
	 *
	 * @param url             URL to request. It must contains full URL.
	 *                        (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param fullAccessToken OneDrive access token.
	 * @return {@link HttpsRequest} object that contains {@code url} and {@code fullAccessToken}.
	 * @throws RuntimeException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                          <tt>"http"</tt> or <tt>"https"</tt>.
	 */
	@NotNull
	public static HttpsRequest newOneDriveRequest(@NotNull URL url, @NotNull String fullAccessToken) {
		HttpsRequest request = new HttpsRequest(url);
		request.setHeader("Authorization", fullAccessToken);
		return request;
	}





	/* *******************************************
	 *
	 *           Non Blocking Member
	 *
	 *********************************************/


	@NotNull
	public HttpsClientHandler doAsync(@NotNull String api, @NotNull HttpMethod method) {
		try {
			HttpsClient httpsClient = new HttpsClient(group, new URI(BASE_URL + api), method);
			httpsClient.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
			return httpsClient.send();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".");
		}
	}

	@NotNull
	public HttpsClientHandler doAsync(@NotNull URI uri, @NotNull HttpMethod method) {
		HttpsClient httpsClient = new HttpsClient(group, uri, method);
		httpsClient.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		return httpsClient.send();
	}

	@NotNull
	public HttpsClientHandler doAsync(@NotNull String api, @NotNull HttpMethod method,
									  @NotNull AsyncHttpsResponseHandler onComplete) {
		try {
			HttpsClient httpsClient = new HttpsClient(group, new URI(BASE_URL + api), method, onComplete);
			httpsClient.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
			return httpsClient.send();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Wrong api : \"" + api + "\", full URL : \"" + BASE_URL + api + "\".");
		}
	}

	@NotNull
	public HttpsClientHandler doAsync(@NotNull URI uri, @NotNull HttpMethod method,
									  @NotNull AsyncHttpsResponseHandler onComplete) {
		HttpsClient httpsClient = new HttpsClient(group, uri, method, onComplete);
		httpsClient.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		return httpsClient.send();
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
	public ObjectNode doGetJson(@NotNull String api) {
		HttpsResponse response = doGet(api, client.getFullToken());

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	/**
	 * <a href='https://dev.onedrive.com/items/get.htm'>https://dev.onedrive.com/items/get.htm</a>
	 * <br><br>
	 * Instantly send GET request to {@code url}, and return parsed JSON response object.
	 * <br><br>
	 * It is assured that the return value is always not {@code null}, if the response is successfully received.
	 * (when 200 OK or even non-OK response like 404 NOT FOUND or something is received).
	 * <br>
	 * But if other error happens while requesting (for example network error, bad api, wrong token... etc.),
	 * it will throw {@link RuntimeException}.
	 * <br><br>
	 * {@code api} must contains full URL.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.doGetJson(new URL("https://api.onedrive.com/v1.0/drive/items/{item-id}"), "AAD....2XA")
	 * }
	 *
	 * @param url URL to request. It must contains full URL.
	 *            (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @return Object that parsed from HTTP GET's json response.
	 * @throws RuntimeException If {@code url} form is incorrect or connection fails.
	 * @see ObjectNode
	 */
	public ObjectNode doGetJson(@NotNull URL url) {
		HttpsResponse response = doGet(url, client.getFullToken());

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public ObjectNode doPostJsonResponse(@NotNull String api, @NotNull String content) {
		return doPostJsonResponse(api, content.getBytes(StandardCharsets.UTF_8));
	}

	public ObjectNode doPostJsonResponse(@NotNull String api, @NotNull byte[] content) {
		HttpsResponse response = doPost(api, client.getFullToken(), content);

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	public ObjectNode doPostJsonResponse(@NotNull URL url, @NotNull String content) {
		return doPostJsonResponse(url, content.getBytes(StandardCharsets.UTF_8));
	}

	public ObjectNode doPostJsonResponse(@NotNull URL url, @NotNull byte[] content) {
		HttpsResponse response = doPost(url, client.getFullToken(), content);

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	public <T> T doGetObject(@NotNull String api, Class<T> classType) {
		HttpsResponse response = doGet(api, client.getFullToken());

		try {
			return mapper.readValue(response.getContent(), classType);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Can't convert response to " + classType + ".");
		}
	}

	public HttpsResponse postMetadata(@NotNull String api, byte[] content) {
		HttpsRequest request = newOneDriveRequest(api, client.getFullToken());
		request.setHeader("Content-Type", "application/json");
		request.setHeader("Prefer", "respond-async");
		return request.doPost(content);
	}

	@NotNull
	public HttpsClientHandler patchMetadata(@NotNull String api, byte[] content) {
		HttpsClientHandler clientHandler = patchMetadataAsync(api, content, null);

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
	public HttpsClientHandler patchMetadataAsync(@NotNull String api, byte[] content) {
		return patchMetadataAsync(api, content, null);
	}

	@NotNull
	@SneakyThrows(URISyntaxException.class)
	public HttpsClientHandler patchMetadataAsync(@NotNull String api, byte[] content,
												 AsyncHttpsResponseHandler handler) {
		HttpsClient httpsClient = new HttpsClient(group, new URI(BASE_URL + api), HttpMethod.PATCH, handler);

		httpsClient.setHeader(HttpHeaderNames.AUTHORIZATION, client.getFullToken());
		httpsClient.setHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
		httpsClient.setHeader(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(content.length));

		return httpsClient.send(content);
	}
}
