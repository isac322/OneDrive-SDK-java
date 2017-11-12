package com.bhyoo.onedrive.container.facet;

import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.items.ItemReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

// TODO: Enhance javadoc

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
	@Getter protected final @Nullable FileSystemInfoFacet fileSystemInfo;
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

	@JsonCreator
	protected RemoteItemFacet(@NotNull @JsonProperty("id") String id,
							  @Nullable @JsonProperty("createdBy") IdentitySet creator,
							  @Nullable @JsonProperty("createdDateTime") String createdDateTime,
							  @Nullable @JsonProperty("folder") FolderFacet folder,
							  @Nullable @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
							  @Nullable @JsonProperty("file") FileFacet file,
							  @Nullable @JsonProperty("lastModifiedBy") IdentitySet lastModifier,
							  @Nullable @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
							  @Nullable @JsonProperty("name") String name,
							  @Nullable @JsonProperty("package") PackageFacet packages,
							  @NotNull @JsonProperty("parentReference") ItemReference parentReference,
							  @Nullable @JsonProperty("shared") SharedFacet shared,
							  @Nullable @JsonProperty("sharepointIds") SharePointIdsFacet sharepointIds,
							  @JsonProperty("size") Long size,
							  @Nullable @JsonProperty("webDavUrl") URI webDavUrl,
							  @NotNull @JsonProperty("webUrl") URI webUrl) {
		assert size != null : "size field is null in RemoteItemFacet!!";

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
}
