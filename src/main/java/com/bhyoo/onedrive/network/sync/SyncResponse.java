package com.bhyoo.onedrive.network.sync;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SyncResponse {
	@Getter protected final URL url;
	@Getter protected final int code;
	@Getter protected final String message;
	@Getter protected final Map<String, List<String>> header;
	@Getter protected final ByteBuf contentBuf;
	protected String contentString;

	public SyncResponse(URL url, int code, String message,
						Map<String, List<String>> header, ByteBuf contentBuf) {
		this.url = url;
		this.code = code;
		this.message = message;
		this.header = header;
		this.contentBuf = contentBuf;
	}

	/**
	 * For programmer's convenience, return decoded HTTP response's body as {@code String}.<br>
	 * Always decode with <b>UTF-8</b>.<br>
	 * If you want to decode the body with another encoding, you should call {@code getContent()} and decode manually.
	 *
	 * @return HTTP response body as {@code String}. Body is forced to decode with UTF-8.
	 */
	@NotNull
	public String getContentString() {
		if (contentString == null) {
			contentString = new String(contentBuf.array(), 0, contentBuf.readableBytes(), StandardCharsets.UTF_8);
		}

		return contentString;
	}

	public int getLength() {
		return contentBuf.readableBytes();
	}

	public byte[] getContent() {
		return getContent(true);
	}

	public byte[] getContent(boolean autoRelease) {
		byte[] array = contentBuf.array();
		if (autoRelease) release();
		return array;
	}

	public void release() {
		contentBuf.release();
	}
}
