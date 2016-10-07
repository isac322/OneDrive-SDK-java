package org.onedrive.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.network.HttpsRequest;
import org.network.HttpsResponse;
import org.onedrive.Client;

import java.io.IOException;
import java.net.MalformedURLException;
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
	@Getter private static final String baseUrl = "https://api.onedrive.com/v1.0";
	private final ObjectMapper mapper;
	@Getter private final Client client;


	public OneDriveRequest(@NotNull Client client, @NotNull ObjectMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}


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
	 * @param api         API to get. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *                    <tt>/drive/root:/{path}</tt>)
	 * @param accessToken OneDrive access token.
	 * @return HTTP GET's response object.
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see HttpsRequest#doGet()
	 */
	@NotNull
	public static HttpsResponse doGet(@NotNull String api, @NotNull String accessToken) {
		return newOneDriveRequest(api, accessToken).doGet();
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
	 * @param url         URL to get. It must contains full URL
	 *                    (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param accessToken OneDrive access token.
	 * @return HTTP GET's response object.
	 * @throws RuntimeException If {@code url} form is incorrect or connection fails.
	 * @see HttpsRequest#doGet()
	 */
	@NotNull
	public static HttpsResponse doGet(@NotNull URL url, @NotNull String accessToken) {
		return newOneDriveRequest(url, accessToken).doGet();
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull String api, @NotNull String accessToken, @NotNull byte[] content) {
		return newOneDriveRequest(api, accessToken).doPost(content);
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull String api, @NotNull String accessToken, @NotNull String content) {
		return newOneDriveRequest(api, accessToken).doPost(content);
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull URL url, @NotNull String accessToken, @NotNull byte[] content) {
		return newOneDriveRequest(url, accessToken).doPost(content);
	}

	@NotNull
	public static HttpsResponse doPost(@NotNull URL url, @NotNull String accessToken, @NotNull String content) {
		return newOneDriveRequest(url, accessToken).doPost(content);
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
	 * @param api         API to delete. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *                    <tt>/drive/root:/{path}</tt>)
	 * @param accessToken OneDrive access token.
	 * @return HTTP DELETE's response object.
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see HttpsRequest#doDelete()
	 */
	@NotNull
	public static HttpsResponse doDelete(@NotNull String api, @NotNull String accessToken) {
		return newOneDriveRequest(api, accessToken).doDelete();
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
	 * @param url         URL to delete. It must contains full URL
	 *                    (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param accessToken OneDrive access token.
	 * @return HTTP DELETE's response object.
	 * @throws RuntimeException If {@code url} form is incorrect or connection fails.
	 * @see HttpsRequest#doDelete()
	 */
	@NotNull
	public static HttpsResponse doDelete(@NotNull URL url, @NotNull String accessToken) {
		return newOneDriveRequest(url, accessToken).doDelete();
	}


	/**
	 * Make {@link HttpsRequest} object with given {@code api} and {@code accessToken} for programmer's convenience.
	 * <br><br>
	 * {@code api} must fallow API form.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.newOneDriveRequest("/drives", "AAD....2XA")
	 * }
	 *
	 * @param api         API to request. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *                    <tt>/drive/root:/{path}</tt>)
	 * @param accessToken OneDrive access token.
	 * @return {@link HttpsRequest} object that contains {@code api} and {@code accessToken}.
	 * @throws RuntimeException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                          <tt>"http"</tt> or <tt>"https"</tt>.
	 */
	@NotNull
	public static HttpsRequest newOneDriveRequest(@NotNull String api, @NotNull String accessToken) {
		try {
			URL requestUrl = new URL(baseUrl + api);
			return newOneDriveRequest(requestUrl, accessToken);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Code error. Wrong URL form. Should check code's String: \"" + baseUrl + api + "\"");
		}
	}


	/**
	 * Make {@link HttpsRequest} object with given {@code url} and {@code accessToken} for programmer's convenience.
	 * <br><br>
	 * {@code url} must contains full URL.
	 * <br>
	 * Example:<br>
	 * {@code
	 * OneDriveRequest.newOneDriveRequest(new URL("https://api.onedrive.com/v1.0/drive/items/{item-id}"), "AAD....2XA")
	 * }
	 *
	 * @param url         URL to request. It must contains full URL.
	 *                    (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param accessToken OneDrive access token.
	 * @return {@link HttpsRequest} object that contains {@code url} and {@code accessToken}.
	 * @throws RuntimeException If api form is invalid. It is mainly because of {@code api} that starting with
	 *                          <tt>"http"</tt> or <tt>"https"</tt>.
	 */
	@NotNull
	public static HttpsRequest newOneDriveRequest(@NotNull URL url, @NotNull String accessToken) {
		HttpsRequest request = new HttpsRequest(url);
		request.setHeader("Authorization", "bearer " + accessToken);
		return request;
	}

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
	 * @param api         API to get. It must starts with <tt>/</tt>, kind of API form. (like <tt>/drives</tt> or
	 *                    <tt>/drive/root:/{path}</tt>)
	 * @param accessToken OneDrive access token.
	 * @return that parsed from HTTP GET's json response.
	 * @throws RuntimeException If {@code api} form is incorrect or connection fails.
	 * @see ObjectNode
	 */
	@NotNull
	public ObjectNode doGetJson(@NotNull String api, @NotNull String accessToken) {
		HttpsResponse response = doGet(api, accessToken);

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
	 * @param url         URL to request. It must contains full URL.
	 *                    (for example <tt>https://api.onedrive.com/v1.0/drive</tt>).
	 * @param accessToken OneDrive access token.
	 * @return Object that parsed from HTTP GET's json response.
	 * @throws RuntimeException If {@code url} form is incorrect or connection fails.
	 * @see ObjectNode
	 */
	@NotNull
	public ObjectNode doGetJson(@NotNull URL url, @NotNull String accessToken) {
		HttpsResponse response = doGet(url, accessToken);

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public ObjectNode doPostJson(@NotNull String api, @NotNull String accessToken, @NotNull String content) {
		return doPostJson(api, accessToken, content.getBytes(StandardCharsets.UTF_8));
	}

	@NotNull
	public ObjectNode doPostJson(@NotNull String api, @NotNull String accessToken, @NotNull byte[] content) {
		HttpsResponse response = doPost(api, accessToken, content);

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public ObjectNode doPostJson(@NotNull URL url, @NotNull String accessToken, @NotNull String content) {
		return doPostJson(url, accessToken, content.getBytes(StandardCharsets.UTF_8));
	}

	@NotNull
	public ObjectNode doPostJson(@NotNull URL url, @NotNull String accessToken, @NotNull byte[] content) {
		HttpsResponse response = doPost(url, accessToken, content);

		try {
			return (ObjectNode) mapper.readTree(response.getContent());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public <T> T doGetObject(@NotNull String api, @NotNull String accessToken, Class<T> classType) {
		HttpsResponse response = doGet(api, accessToken);

		try {
			return mapper.readValue(response.getContent(), classType);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Can't convert response to " + classType + ".");
		}
	}

	@NotNull
	public HttpsResponse postMetadata(@NotNull String api, @NotNull String accessToken, byte[] content) {
		HttpsRequest request = newOneDriveRequest(api, accessToken);
		request.setHeader("Content-Type", "application/json");
		request.setHeader("Prefer", "respond-async");
		return request.doPost(content);
	}
}
