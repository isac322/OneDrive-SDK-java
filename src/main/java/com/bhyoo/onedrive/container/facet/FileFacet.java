package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/file_facet.htm">https://dev.onedrive.com/facets/file_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FileFacet {
	@Getter @Setter(PRIVATE) @Nullable protected String mimeType;
	@Setter(PRIVATE) @JsonProperty @Nullable protected Hashes hashes;
	@Getter @Setter(PRIVATE) @Nullable protected Boolean processingMetadata;

	@JsonIgnore public @Nullable String getSha1Hash() {return hashes == null ? null : hashes.sha1Hash;}

	@JsonIgnore public @Nullable String getCrc32Hash() {return hashes == null ? null : hashes.crc32Hash;}

	@JsonIgnore public @Nullable String getQuickXorHash() {return hashes == null ? null : hashes.quickXorHash;}


	@SuppressWarnings("WeakerAccess")
	private static class Hashes {
		public String sha1Hash, crc32Hash, quickXorHash;
	}
}
