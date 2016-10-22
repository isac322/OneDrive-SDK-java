package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

/**
 * https://dev.onedrive.com/facets/file_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileFacet {
	@Getter @Nullable protected final String mimeType;
	@Getter @Nullable protected final String sha1Hash;
	@Getter @Nullable protected final String crc32Hash;
	@Getter @Nullable protected final String quickXorHash;

	@JsonCreator
	protected FileFacet(@Nullable @JsonProperty("mimeType") String mimeType,
						@Nullable @JsonProperty("hashes") LinkedHashMap<String, String> hashes) {
		this.mimeType = mimeType;
		if (hashes != null) {
			this.sha1Hash = hashes.get("sha1Hash");
			this.crc32Hash = hashes.get("crc32Hash");
			this.quickXorHash = hashes.get("quickXorHash");
		}
		else {
			sha1Hash = crc32Hash = quickXorHash = null;
		}
	}
}
