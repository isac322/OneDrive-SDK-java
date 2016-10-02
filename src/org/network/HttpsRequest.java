package org.network;

import com.sun.istack.internal.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Created by isac322 on 16. 9. 30.
 */
public class HttpsRequest {
	protected final HttpURLConnection httpConnection;

	public HttpsRequest(@NotNull String url) throws IOException {
		this(new URL(url));
	}

	public HttpsRequest(@NotNull URL url) throws IOException {
		httpConnection = (HttpURLConnection) url.openConnection();
	}

	public void setHeader(@NotNull String key, String value) {
		httpConnection.setRequestProperty(key, value);
	}

	public HttpsResponse doPost(String content) throws IOException {
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("POST");

		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		httpConnection.setFixedLengthStreamingMode(bytes.length);

		OutputStream out = httpConnection.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();

		return makeResponse();
	}

	public HttpsResponse doGet() throws IOException {
		httpConnection.setRequestMethod("GET");
		return makeResponse();
	}

	protected HttpsResponse makeResponse() throws IOException {
		int code = httpConnection.getResponseCode();
		String message = httpConnection.getResponseMessage();
		Map<String, List<String>> header = httpConnection.getHeaderFields();
		URL url = httpConnection.getURL();

		BufferedInputStream body = new BufferedInputStream(httpConnection.getInputStream());
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		int bytes;
		while ((bytes = body.read()) != -1) {
			byteStream.write(bytes);
		}

		byteStream.close();
		body.close();
		httpConnection.disconnect();
		return new HttpsResponse(url, code, message, header, byteStream.toByteArray());
	}
}
