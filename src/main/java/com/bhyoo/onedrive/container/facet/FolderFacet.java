package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * <a href="https://docs.microsoft.com/ko-kr/onedrive/developer/rest-api/resources/folderview">
 * https://docs.microsoft.com/ko-kr/onedrive/developer/rest-api/resources/folderview
 * </a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FolderFacet {
	@Getter protected final long childCount;
	// Nullable on onedrive for business
	@Getter protected final @Nullable FolderViewFacet view;

	protected FolderFacet(@NotNull Long childCount, @Nullable FolderViewFacet view) {
		this.childCount = childCount;
		this.view = view;
	}

	public static FolderFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable Long childCount = null;
		@Nullable FolderViewFacet view = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "childCount":
					childCount = parser.getLongValue();
					break;
				case "view":
					view = FolderViewFacet.deserialize(parser);
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in FolderFacet : " + currentName);
			}
		}

		assert childCount != null : "childCount is null";

		return new FolderFacet(childCount, view);
	}
}
