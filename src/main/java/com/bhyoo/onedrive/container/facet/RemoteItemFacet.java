package com.bhyoo.onedrive.container.facet;

import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.items.ItemReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * <a href="https://dev.onedrive.com/facets/remoteitem_facet.htm">https://dev.onedrive.com/facets/remoteitem_facet
 * .htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class RemoteItemFacet {
	@Getter protected final @NotNull String id;

	@JsonProperty("createdBy")
	@Getter protected final @Nullable IdentitySet creator;

	// TODO: convert datetime to some appreciate object
	@Getter protected final @Nullable String createdDateTime;
	@Getter protected final @Nullable FolderFacet folder;
	@Getter protected final @NotNull FileSystemInfoFacet fileSystemInfo;
	@Getter protected final @Nullable FileFacet file;

	@JsonProperty("lastModifiedBy")
	@Getter protected final @Nullable IdentitySet lastModifier;

	// TODO: convert datetime to some appreciate object
	@Getter protected final @Nullable String lastModifiedDateTime;
	@Getter protected final @Nullable String name;

	@JsonProperty("package")
	@Getter protected final @Nullable PackageFacet packages;
	@Getter protected final @NotNull ItemReference parentReference;
	@Getter protected final @Nullable SharedFacet shared;
	@Getter protected final @Nullable SharePointIdsFacet sharepointIds;
	@Getter protected final long size;
	@Getter protected final @Nullable URI webDavUrl;
	@Getter protected final @NotNull URI webUrl;


	protected RemoteItemFacet(@NotNull String id, @Nullable IdentitySet creator, @Nullable String createdDateTime,
							  @Nullable FolderFacet folder, @NotNull FileSystemInfoFacet fileSystemInfo,
							  @Nullable FileFacet file, @Nullable IdentitySet lastModifier,
							  @Nullable String lastModifiedDateTime, @Nullable String name,
							  @Nullable PackageFacet packages, @NotNull ItemReference parentReference,
							  @Nullable SharedFacet shared, @Nullable SharePointIdsFacet sharepointIds,
							  @NotNull Long size, @Nullable URI webDavUrl, @NotNull URI webUrl) {
		this.id = id;
		this.creator = creator;
		this.createdDateTime = createdDateTime;
		this.folder = folder;
		this.fileSystemInfo = fileSystemInfo;
		this.file = file;
		this.lastModifier = lastModifier;
		this.lastModifiedDateTime = lastModifiedDateTime;
		this.name = name;
		this.packages = packages;
		this.parentReference = parentReference;
		this.shared = shared;
		this.sharepointIds = sharepointIds;
		this.size = size;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;
	}

	@SneakyThrows(URISyntaxException.class)
	public static RemoteItemFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull String id = null;
		@Nullable IdentitySet creator = null;

		// TODO: convert datetime to some appreciate object
		@Nullable String createdDateTime = null;
		@Nullable FolderFacet folder = null;
		@Nullable FileSystemInfoFacet fileSystemInfo = null;
		@Nullable FileFacet file = null;

		@Nullable IdentitySet lastModifier = null;

		// TODO: convert datetime to some appreciate object
		@Nullable String lastModifiedDateTime = null;
		@Nullable String name = null;

		@Nullable PackageFacet packages = null;
		@NotNull ItemReference parentReference = null;
		@Nullable SharedFacet shared = null;
		@Nullable SharePointIdsFacet sharepointIds = null;
		@NotNull Long size = null;
		@Nullable URI webDavUrl = null;
		@NotNull URI webUrl = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "id":
					id = parser.getText();
					break;
				case "createdBy":
					creator = IdentitySet.deserialize(parser);
					break;
				case "createdDateTime":
					createdDateTime = parser.getText();
					break;
				case "folder":
					folder = FolderFacet.deserialize(parser);
					break;
				case "fileSystemInfo":
					fileSystemInfo = FileSystemInfoFacet.deserialize(parser);
					break;
				case "file":
					file = FileFacet.deserialize(parser);
					break;
				case "lastModifiedBy":
					lastModifier = IdentitySet.deserialize(parser);
					break;
				case "lastModifiedDateTime":
					lastModifiedDateTime = parser.getText();
					break;
				case "name":
					name = parser.getText();
					break;
				case "package":
					packages = PackageFacet.deserialize(parser);
					break;
				case "parentReference":
					parentReference = ItemReference.deserialize(parser);
					break;
				case "shared":
					shared = SharedFacet.deserialize(parser);
					break;
				case "sharepointIds":
					sharepointIds = SharePointIdsFacet.deserialize(parser);
					break;
				case "size":
					size = parser.getLongValue();
					break;
				case "webDavUrl":
					webDavUrl = new URI(parser.getText());
					break;
				case "webUrl":
					webUrl = new URI(parser.getText());
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in RemoteItemFacet : " + currentName);
			}
		}

		return new RemoteItemFacet(id, creator, createdDateTime, folder, fileSystemInfo, file, lastModifier,
				lastModifiedDateTime, name, packages, parentReference, shared, sharepointIds, size, webDavUrl, webUrl);
	}
}
