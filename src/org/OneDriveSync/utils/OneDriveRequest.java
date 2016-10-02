package org.OneDriveSync.utils;

import lombok.Getter;
import org.network.HttpsRequest;
import org.network.HttpsResponse;

import java.io.IOException;
import java.net.URL;

/**
 * Created by isac322 on 16. 10. 2.
 */
public class OneDriveRequest {
	@Getter private static final String baseUrl = "https://api.onedrive.com/v1.0";

	private OneDriveRequest() {
	}

	public static HttpsResponse doGet(String url, String accessToken) throws IOException {
		URL requestUrl = new URL(baseUrl + url);
		HttpsRequest request = new HttpsRequest(requestUrl);
		request.setHeader("Authorization", "bearer " + accessToken);
		return request.doGet();
	}
}
