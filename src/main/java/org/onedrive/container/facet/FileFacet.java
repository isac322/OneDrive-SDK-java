package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/file_facet.htm">https://dev.onedrive.com/facets/file_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileFacet {
	@Getter @Setter(PRIVATE) @Nullable private String mimeType;
	@Setter(PRIVATE) @Nullable private Hashes hashes;

	@JsonIgnore public @Nullable String getSha1Hash() {return hashes == null ? null : hashes.sha1Hash;}

	@JsonIgnore public @Nullable String getCrc32Hash() {return hashes == null ? null : hashes.crc32Hash;}

	@JsonIgnore public @Nullable String getQuickXorHash() {return hashes == null ? null : hashes.quickXorHash;}


	@SuppressWarnings("WeakerAccess")
	private static class Hashes {
		public String sha1Hash, crc32Hash, quickXorHash;
	}
}
