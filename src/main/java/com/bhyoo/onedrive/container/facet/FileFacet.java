package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/facets/file_facet.htm">https://dev.onedrive.com/facets/file_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FileFacet {
	@Getter protected final  @Nullable String mimeType;
	protected final @Nullable Hashes hashes;
	@Getter protected final @Nullable Boolean processingMetadata;

	protected FileFacet(@Nullable String mimeType, @Nullable Hashes hashes, @Nullable Boolean processingMetadata) {
		this.mimeType = mimeType;
		this.hashes = hashes;
		this.processingMetadata = processingMetadata;
	}

	public static FileFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String mimeType = null;
		@Nullable Hashes hashes = null;
		@Nullable Boolean processingMetadata = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "mimeType":
					mimeType = parser.getText();
					break;
				case "hashes":
					hashes = Hashes.deserialize(parser);
					break;
				case "processingMetadata":
					processingMetadata = parser.getBooleanValue();
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in FileFacet : " + currentName);
			}
		}

		return new FileFacet(mimeType, hashes, processingMetadata);
	}

	@JsonIgnore public @Nullable String getSha1Hash() {return hashes == null ? null : hashes.sha1Hash;}

	@JsonIgnore public @Nullable String getCrc32Hash() {return hashes == null ? null : hashes.crc32Hash;}

	@JsonIgnore public @Nullable String getQuickXorHash() {return hashes == null ? null : hashes.quickXorHash;}


	private static class Hashes {
		public String sha1Hash, crc32Hash, quickXorHash;

		static Hashes deserialize(@NotNull JsonParser parser) throws IOException {
			Hashes ret = new Hashes();

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String currentName = parser.getCurrentName();
				parser.nextToken();

				switch (currentName) {
					case "sha1Hash":
						ret.sha1Hash = parser.getText();
						break;
					case "crc32Hash":
						ret.crc32Hash = parser.getText();
						break;
					case "quickXorHash":
						ret.quickXorHash = parser.getText();
						break;
					default:
						throw new IllegalStateException("Unknown attribute detected in Hashes : " + currentName);
				}
			}

			return ret;
		}
	}
}
