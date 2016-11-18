package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * <a href="https://dev.onedrive.com/facets/file_facet.htm">https://dev.onedrive.com/facets/file_facet.htm</a>
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileFacet {
	@Getter @Nullable protected final String mimeType;
	@Nullable @JsonProperty protected final Hashes hashes;

	@JsonCreator
	protected FileFacet(@Nullable @JsonProperty("mimeType") String mimeType,
						@Nullable @JsonProperty("hashes") Hashes hashes) {
		this.mimeType = mimeType;
		this.hashes = hashes;
	}

	@Nullable
	@JsonIgnore
	public String getSha1Hash() {
		return hashes == null ? null : hashes.sha1Hash;
	}

	@Nullable
	@JsonIgnore
	public String getCrc32Hash() {
		return hashes == null ? null : hashes.crc32Hash;
	}

	@Nullable
	@JsonIgnore
	public String getQuickXorHash() {
		return hashes == null ? null : hashes.quickXorHash;
	}


	private static class Hashes {
		@Nullable @JsonProperty private final String sha1Hash;
		@Nullable @JsonProperty private final String crc32Hash;
		@Nullable @JsonProperty private final String quickXorHash;

		public Hashes(@JsonProperty("sha1Hash") @Nullable String sha1Hash,
					  @JsonProperty("crc32Hash") @Nullable String crc32Hash,
					  @JsonProperty("quickXorHash") @Nullable String quickXorHash) {
			this.sha1Hash = sha1Hash;
			this.crc32Hash = crc32Hash;
			this.quickXorHash = quickXorHash;
		}
	}
}
