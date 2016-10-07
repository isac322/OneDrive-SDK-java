package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.network.HttpsRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * https://dev.onedrive.com/resources/itemReference.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ItemReference {
	@Getter @NotNull protected final String driveId;
	@Getter @Nullable protected final String id;
	@Getter @Nullable protected final String path;
	@Getter @NotNull protected final String rawPath;

	@JsonCreator
	protected ItemReference(@JsonProperty("driveId") String driveId,
							@JsonProperty("id") String id,
							@JsonProperty("path") String path) {
		this.driveId = driveId;
		this.id = id;
		this.rawPath = path;

		if (path != null) {
			String decoded;
			try {
				decoded = URLDecoder.decode(path, "UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				this.path = null;
				throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Bad encoding on path");
			}

			this.path = decoded;
		}
		else this.path = null;
	}
}
