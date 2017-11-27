package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <a href="https://dev.onedrive.com/facets/sharepointIds_facet.htm">https://dev.onedrive
 * .com/facets/sharepointIds_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SharePointIdsFacet {
	@Getter protected final @Nullable String listId;
	@Getter protected final @Nullable String listItemId;
	@Getter protected final @Nullable String listItemUniqueId;
	@Getter protected final @Nullable String siteId;
	@Getter protected final @Nullable URI siteUrl;
	@Getter protected final @Nullable String webId;

	protected SharePointIdsFacet(@Nullable String listId, @Nullable String listItemId,
								 @Nullable String listItemUniqueId, @Nullable String siteId, @Nullable URI siteUrl,
								 @Nullable String webId) {
		this.listId = listId;
		this.listItemId = listItemId;
		this.listItemUniqueId = listItemUniqueId;
		this.siteId = siteId;
		this.siteUrl = siteUrl;
		this.webId = webId;
	}

	@SneakyThrows(URISyntaxException.class)
	public static SharePointIdsFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String listId = null;
		@Nullable String listItemId = null;
		@Nullable String listItemUniqueId = null;
		@Nullable String siteId = null;
		@Nullable URI siteUrl = null;
		@Nullable String webId = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "listId":
					listId = parser.getText();
					break;
				case "listItemId":
					listItemId = parser.getText();
					break;
				case "listItemUniqueId":
					listItemUniqueId = parser.getText();
					break;
				case "siteId":
					siteId = parser.getText();
					break;
				case "siteUrl":
					siteUrl = new URI(parser.getText());
					break;
				case "webId":
					webId = parser.getText();
					break;
				default:
					throw new IllegalStateException(
							"Unknown attribute detected in SharePointIdsFacet : " + currentName);
			}
		}

		return new SharePointIdsFacet(listId, listItemId, listItemUniqueId, siteId, siteUrl, webId);
	}
}
