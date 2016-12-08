package org.onedrive.network.sync;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.AsciiString;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SyncRequest {
	public static final String NETWORK_ERR_MSG = "Network connection error. Please retry later or contact API author.";
	private final HttpsURLConnection httpConnection;

	@SneakyThrows(MalformedURLException.class)
	public SyncRequest(@NotNull String url) {
		URL url1 = new URL(url);
		try {
			httpConnection = (HttpsURLConnection) url1.openConnection();
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException(NETWORK_ERR_MSG, e);
		}
	}

	public SyncRequest(@NotNull URL url) {
		try {
			httpConnection = (HttpsURLConnection) url.openConnection();
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException(NETWORK_ERR_MSG, e);
		}
	}

	/**
	 * Add {@code key}, {@code value} pair to http request's header.<br>
	 * Like: {@code key}: {@code value}.
	 *
	 * @param key   Key to add in request's header.
	 * @param value Value to add in request's header. It could be {@code null}.
	 */
	public SyncRequest setHeader(@NotNull String key, String value) {
		httpConnection.setRequestProperty(key, value);
		return this;
	}

	public SyncRequest setHeader(@NotNull AsciiString key, String value) {
		httpConnection.setRequestProperty(key.toString(), value);
		return this;
	}

	@NotNull
	public SyncResponse doPost(String content) {
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		return doPost(bytes);
	}

	@NotNull
	public SyncResponse doPost(byte[] content) {
		try {
			httpConnection.setRequestMethod("POST");
			return sendContent(content);
		}
		catch (ProtocolException e) {
			throw new UnsupportedOperationException("unsupported method POST. contact author with stacktrace", e);
		}
	}

	@NotNull
	public SyncResponse doPut(String content) {
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		return doPost(bytes);
	}

	@NotNull
	public SyncResponse doPut(byte[] content) {
		try {
			httpConnection.setRequestMethod("PUT");
			return sendContent(content);
		}
		catch (ProtocolException e) {
			throw new UnsupportedOperationException("unsupported method PUT. contact author with stacktrace", e);
		}
	}

	public SyncResponse sendContent(byte[] content) {
		try {
			httpConnection.setDoOutput(true);

			httpConnection.setFixedLengthStreamingMode(content.length);

			OutputStream out = httpConnection.getOutputStream();
			out.write(content);
			out.flush();
			out.close();

			return makeResponse();
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException(NETWORK_ERR_MSG, e);
		}
	}

	@NotNull
	public SyncResponse doDelete() {
		try {
			httpConnection.setRequestMethod("DELETE");
			return makeResponse();
		}
		catch (ProtocolException e) {
			throw new UnsupportedOperationException("unsupported method DELETE. contact author with stacktrace", e);
		}
	}

	@NotNull
	public SyncResponse doGet() {
		try {
			httpConnection.setRequestMethod("GET");
			return makeResponse();
		}
		catch (ProtocolException e) {
			throw new UnsupportedOperationException("unsupported method GET. contact author with stacktrace", e);
		}
	}

	/**
	 * @return Response object.
	 * @throws RuntimeException fail to network connection or fail to read response.
	 */
	@NotNull
	protected SyncResponse makeResponse() {
		try {
			int code = httpConnection.getResponseCode();
			String message = httpConnection.getResponseMessage();
			Map<String, List<String>> header = httpConnection.getHeaderFields();
			URL url = httpConnection.getURL();

			ByteBuf byteStream = Unpooled.buffer(512);
			InputStream body;

			if (code < 400)
				body = httpConnection.getInputStream();
			else
				body = httpConnection.getErrorStream();

			byte[] buffer = new byte[512];

			int readBytes;
			while ((readBytes = body.read(buffer)) >= 0) {
				byteStream.writeBytes(buffer, 0, readBytes);
			}
			body.close();
			return new SyncResponse(url, code, message, header, byteStream.array(), byteStream.writerIndex());

			// which one is better?
			/*
			val byteStream = new ByteArrayOutputStream();
			BufferedInputStream body;

			if (code < 400)
				body = new BufferedInputStream(httpConnection.getInputStream());
			else
				body = new BufferedInputStream(httpConnection.getErrorStream());

			int bytes;
			while ((bytes = body.read()) != -1) {
				byteStream.write(bytes);
			}

			byteStream.close();
			body.close();
			return new SyncResponse(url, code, message, header, byteStream.toByteArray());
			*/
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException(NETWORK_ERR_MSG, e);
		}
		finally {
			httpConnection.disconnect();
		}
	}
}
