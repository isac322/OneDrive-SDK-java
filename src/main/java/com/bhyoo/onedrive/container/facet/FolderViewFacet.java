package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/facets/folder_facet.htm">https://dev.onedrive.com/facets/folder_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FolderViewFacet {
	@Getter protected final @NotNull SortType sortBy;
	@Getter protected final @NotNull SortOrderType sortOrder;
	@Getter protected final @NotNull ViewType viewType;

	protected FolderViewFacet(@NotNull SortType sortBy, @NotNull SortOrderType sortOrder, @NotNull ViewType viewType) {
		this.sortBy = sortBy;
		this.sortOrder = sortOrder;
		this.viewType = viewType;
	}

	public static FolderViewFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable SortType sortBy = null;
		@Nullable SortOrderType sortOrder = null;
		@Nullable ViewType viewType = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "sortBy":
					sortBy = SortType.deserialize(parser.getText());
					break;
				case "sortOrder":
					sortOrder = SortOrderType.deserialize(parser.getText());
					break;
				case "viewType":
					viewType = ViewType.deserialize(parser.getText());
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in FolderViewFacet : " + currentName);
			}
		}

		assert sortBy != null;
		assert sortOrder != null;
		assert viewType != null;

		return new FolderViewFacet(sortBy, sortOrder, viewType);
	}
}
