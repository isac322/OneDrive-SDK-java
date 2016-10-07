package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
 import org.jetbrains.annotations.NotNull;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
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
	@Getter(onMethod = @__(@JsonIgnore)) @Nullable protected final String path;
	@Getter @Nullable @JsonProperty("path") protected final String rawPath;

	@JsonCreator
	protected ItemReference(@JsonProperty("driveId") @NotNull String driveId,
							@JsonProperty("id") @Nullable String id,
							@JsonProperty("path") @Nullable String rawPath) {
		this.driveId = driveId;
		this.id = id;
		this.rawPath = rawPath;

		if (rawPath != null) {
			String decoded;
			try {
				decoded = URLDecoder.decode(rawPath, "UTF-8");
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
