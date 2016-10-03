package org.network;

import com.sun.istack.internal.NotNull;
import lombok.Getter;

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
public class HttpsResponse {
	@Getter protected final URL url;
	@Getter protected final int code;
	@Getter protected final String message;
	@Getter protected final Map<String, List<String>> header;
	@Getter protected final byte[] content;
	protected String contentString;

	public HttpsResponse(URL url, int code, String message, Map<String, List<String>> header, byte[] content) {
		this.url = url;
		this.code = code;
		this.message = message;
		this.header = header;
		this.content = content;
	}

	/**
	 * For programmer's convenience, return decoded HTTP response's body as {@code String}.<br>
	 * Always decode with <b>UTF-8</b>.<br>
	 * If you want to decode body with another encoding, you should call {@code getContent()} and decode manually.
	 *
	 * @return HTTP response body as {@code String}. Body is forced to decode with UTF-8.
	 */
	@NotNull
	public String getContentString() {
		if (contentString == null) {
			contentString = new String(content, StandardCharsets.UTF_8);
		}

		return contentString;
	}
}
