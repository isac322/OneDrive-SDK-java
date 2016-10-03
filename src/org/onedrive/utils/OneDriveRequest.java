package org.onedrive.utils;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.network.HttpsRequest;
import org.network.HttpsResponse;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 2.
 *
 * @author isac322
 */
public class OneDriveRequest {
	@Getter private static final String baseUrl = "https://api.onedrive.com/v1.0";
	private static final JSONParser parser = new JSONParser();

	@Deprecated
	private OneDriveRequest() {
	}

	/**
	 * https://dev.onedrive.com/items/delete.htm
	 *
	 * @param url         Id or path that want to get.
	 * @param accessToken OneDrive access token.
	 * @return HTTP GET's response object.
	 * @throws RuntimeException In case {@code url} form is incorrect or connection fails.
	 * @see HttpsRequest#makeResponse()
	 */
	@NotNull
	public static HttpsResponse doGet(@NotNull String url, @NotNull String accessToken) {
		return makeRequest(url, accessToken).doGet();
	}


	/**
	 * https://dev.onedrive.com/items/delete.htm
	 *
	 * @param url         Id or path that want to remove.
	 * @param accessToken OneDrive access token.
	 * @return HTTP DELETE's response object.
	 * @throws RuntimeException In case {@code url} form is incorrect or connection fails.
	 * @see HttpsRequest#makeResponse()
	 */
	@NotNull
	public static HttpsResponse doDelete(@NotNull String url, @NotNull String accessToken) {
		return makeRequest(url, accessToken).doDelete();
	}


	@NotNull
	public static HttpsRequest makeRequest(@NotNull String url, @NotNull String accessToken) {
		try {
			URL requestUrl = new URL(baseUrl + url);
			HttpsRequest request = new HttpsRequest(requestUrl);
			request.setHeader("Authorization", "bearer " + accessToken);
			return request;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException("Code error. Wrong URL form. Should check code's String.");
		}
	}

	@NotNull
	public static JSONObject getJsonResponse(@NotNull String url, @NotNull String accessToken) {
		HttpsResponse response = doGet(url, accessToken);
		try {
			// TODO: handling not 200 OK response.
			return (JSONObject) parser.parse(response.getContentString());
		}
		catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}
	}
}
