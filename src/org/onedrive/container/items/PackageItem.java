package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;

/**
 * <a href='https://dev.onedrive.com/facets/package_facet.htm'>https://dev.onedrive.com/facets/package_facet.htm</a>
 *
 * Because there is only one package type item in OneDrive now, this class inherits {@link FileItem}.
 *
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonFilter("PackageItem")
@JsonDeserialize(as = PackageItem.class)
public class PackageItem extends FileItem {
	@Getter @NotNull protected PackageFacet packages;

	@JsonCreator
	public PackageItem(@JacksonInject("OneDriveClient") Client client,
					   @JsonProperty("id") String id,
					   @JsonProperty("audio") @Nullable AudioFacet audio,
					   @JsonProperty("createdBy") IdentitySet createdBy,
					   @JsonProperty("createdDateTime") String createdDateTime,
					   @JsonProperty("cTag") String cTag,
					   @JsonProperty("deleted") ObjectNode deleted,
					   @JsonProperty("description") String description,
					   @JsonProperty("eTag") String eTag,
					   @JsonProperty("file") @NotNull FileFacet file,
					   @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
					   @JsonProperty("image") @Nullable ImageFacet image,
					   @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
					   @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
					   @JsonProperty("location") @Nullable LocationFacet location,
					   @JsonProperty("name") @NotNull String name,
					   @JsonProperty("parentReference") @NotNull ItemReference parentReference,
					   @JsonProperty("photo") @Nullable PhotoFacet photo,
					   @JsonProperty("searchResult") @Nullable SearchResultFacet searchResult,
					   @JsonProperty("shared") @Nullable SharedFacet shared,
					   @JsonProperty("sharePointIds") @Nullable SharePointIdsFacet sharePointIds,
					   @JsonProperty("size") long size,
					   @JsonProperty("video") @Nullable VideoFacet video,
					   @JsonProperty("webDavUrl") String webDavUrl,
					   @JsonProperty("webUrl") String webUrl,
					   @JsonProperty("package") @NotNull PackageFacet packages) {
		super(client, id, audio, createdBy, createdDateTime, cTag, deleted, description, eTag, file, fileSystemInfo,
				image, lastModifiedBy, lastModifiedDateTime, location, name, parentReference, photo, searchResult,
				shared, sharePointIds, size, video, webDavUrl, webUrl);

		this.packages = packages;
	}
}
