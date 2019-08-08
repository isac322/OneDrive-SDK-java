package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

public class ThumbnailSet {
	@Getter protected final @NotNull String id;
	@Getter protected final @Nullable Thumbnail large;
	@Getter protected final @Nullable Thumbnail medium;
	@Getter protected final @Nullable Thumbnail source;
	@Getter protected final @Nullable Thumbnail small;

	ThumbnailSet(@NotNull String id, @Nullable Thumbnail large, @Nullable Thumbnail medium,
				 @Nullable Thumbnail source, @Nullable Thumbnail small) {
		this.id = id;
		this.large = large;
		this.medium = medium;
		this.source = source;
		this.small = small;
	}

	public static @NotNull ThumbnailSet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String id = null;
		@Nullable Thumbnail large = null;
		@Nullable Thumbnail medium = null;
		@Nullable Thumbnail source = null;
		@Nullable Thumbnail small = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "id":
					id = parser.getText();
					break;
				case "large":
					large = Thumbnail.deserialize(parser);
					break;
				case "medium":
					medium = Thumbnail.deserialize(parser);
					break;
				case "source":
					source = Thumbnail.deserialize(parser);
					break;
				case "small":
					small = Thumbnail.deserialize(parser);
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in ThumbnailSet : " + currentName);
			}
		}

		assert id != null : "height is null";

		return new ThumbnailSet(id, large, medium, source, small);
	}
}
