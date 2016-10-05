package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;

/**
 * https://dev.onedrive.com/facets/package_facet.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = PackageItem.class)
public class PackageItem extends BaseItem {
	@Getter protected PackageFacet packages;

	@JsonCreator
	public PackageItem(@JsonProperty("id") String id,
					   @JsonProperty("createdBy") IdentitySet createdBy,
					   @JsonProperty("createdDateTime") String createdDateTime,
					   @JsonProperty("cTag") String cTag,
					   @JsonProperty("deleted") Object deleted,
					   @JsonProperty("description") String description,
					   @JsonProperty("eTag") String eTag,
					   @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
					   @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
					   @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
					   @JsonProperty("name") String name,
					   @JsonProperty("package") PackageFacet packages,
					   @JsonProperty("parentReference") ItemReference parentReference,
					   @JsonProperty("remoteItem") RemoteItemFacet remoteItem,
					   @JsonProperty("searchResult") SearchResultFacet searchResult,
					   @JsonProperty("shared") SharedFacet shared,
					   @JsonProperty("sharePointIds") SharePointIdsFacet sharePointIds,
					   @JsonProperty("size") Long size,
					   @JsonProperty("webDavUrl") String webDavUrl,
					   @JsonProperty("webUrl") String webUrl) {
		this.id = id;
		this.createdBy = createdBy;
		this.createdDateTime = BaseContainer.parseDateTime(createdDateTime);
		this.cTag = cTag;
		this.deleted = deleted != null;
		this.description = description;
		this.eTag = eTag;
		this.fileSystemInfo = fileSystemInfo;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDateTime = BaseContainer.parseDateTime(lastModifiedDateTime);
		this.name = name;
		this.packages = packages;
		this.parentReference = parentReference;
		this.remoteItem = remoteItem;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;
	}
}
