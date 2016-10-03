package org.network;

import com.sun.istack.internal.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 9. 30.
 *
 * @author isac322
 */
public class HttpsRequest {
	public static final String NETWORK_ERR_MSG = "Network connection error. Please retry later or contact author.";
	protected final HttpURLConnection httpConnection;

	public HttpsRequest(@NotNull String url) throws MalformedURLException {
		this(new URL(url));
	}

	public HttpsRequest(@NotNull URL url) {
		try {
			httpConnection = (HttpURLConnection) url.openConnection();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	public void setHeader(@NotNull String key, String value) {
		httpConnection.setRequestProperty(key, value);
	}

	@NotNull
	public HttpsResponse doPost(String content) {
		try {
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
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public HttpsResponse doDelete() {
		try {
			httpConnection.setRequestMethod("DELETE");
			return makeResponse();
		}
		catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public HttpsResponse doGet() {
		try {
			httpConnection.setRequestMethod("GET");
			return makeResponse();
		}
		catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	/**
	 * TODO: handling NOT 200 OK response.
	 *
	 * @return Response object.
	 * @throws RuntimeException fail to network connection or fail to read response.
	 */
	@NotNull
	protected HttpsResponse makeResponse() {
		try {
			int code = httpConnection.getResponseCode();
			String message = httpConnection.getResponseMessage();
			Map<String, List<String>> header = httpConnection.getHeaderFields();
			URL url = httpConnection.getURL();

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			BufferedInputStream body = null;

			if (code == 200) {
				body = new BufferedInputStream(httpConnection.getInputStream());
			}
			else {
				// TODO: should be tested about not 4XX response code.
				body = new BufferedInputStream(httpConnection.getErrorStream());
				// TODO: for debug
				throw new RuntimeException("Not 200 OK response received.");
			}

			int bytes;
			while ((bytes = body.read()) != -1) {
				byteStream.write(bytes);
			}

			byteStream.close();
			body.close();
			httpConnection.disconnect();
			return new HttpsResponse(url, code, message, header, byteStream.toByteArray());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}
}
