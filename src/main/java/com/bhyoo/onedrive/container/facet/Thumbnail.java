package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Thumbnail {
	@Getter protected final int height;
	@Getter protected final @NotNull String url;
	@Getter protected final @Nullable String sourceItemId;
	@Getter protected final int width;

	Thumbnail(int height, @NotNull String url, @Nullable String sourceItemId, int width) {
		this.height = height;
		this.url = url;
		this.sourceItemId = sourceItemId;
		this.width = width;
	}

	public static @NotNull Thumbnail deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable Integer height = null;
		@Nullable String url = null;
		@Nullable String sourceItemId = null;
		@Nullable Integer width = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "height":
					height = parser.getIntValue();
					break;
				case "url":
					url = parser.getText();
					break;
				case "sourceItemId":
					sourceItemId = parser.getText();
					break;
				case "width":
					width = parser.getIntValue();
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in Thumbnail : " + currentName);
			}
		}

		assert height != null : "height is null";
		assert url != null : "url is null";
		assert width != null : "width is null";

		return new Thumbnail(height, url, sourceItemId, width);
	}
}
