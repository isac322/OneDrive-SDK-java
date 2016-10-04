package org.onedrive.container.items;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;

import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/facets/package_facet.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class PackageItem extends BaseItem {
	@Getter protected PackageFacet packages;

	public PackageItem(Client client, String id, IdentitySet createdBy, ZonedDateTime createdDateTime, String cTag,
					   boolean deleted, String description, String eTag, FileSystemInfoFacet fileSystemInfo,
					   IdentitySet lastModifiedBy, ZonedDateTime lastModifiedDateTime, String name,
					   PackageFacet packages, ItemReference parentReference, RemoteItemFacet remoteItem,
					   SearchResultFacet searchResult, SharedFacet shared, SharePointIdsFacet sharePointIds, Long size,
					   String webDavUrl, String webUrl) {
		this.client = client;
		this.id = id;
		this.createdBy = createdBy;
		this.createdDateTime = createdDateTime;
		this.cTag = cTag;
		this.deleted = deleted;
		this.description = description;
		this.eTag = eTag;
		this.fileSystemInfo = fileSystemInfo;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDateTime = lastModifiedDateTime;
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

	@Nullable
	static PackageItem parsePackage(Client client, JSONObject json) {
		if (json == null) return null;

		return new PackageItem(
				client,
				json.getString("id"),
				IdentitySet.parse(json.getObject("createdBy")),
				BaseContainer.parseDateTime(json.getString("createdDateTime")),
				json.getString("cTag"),
				json.getObject("deleted") != null,
				json.getString("description"),
				json.getString("eTag"),
				FileSystemInfoFacet.parse(json.getObject("fileSystemInfo")),
				IdentitySet.parse(json.getObject("lastModifiedBy")),
				BaseContainer.parseDateTime(json.getString("lastModifiedDateTime")),
				json.getString("name"),
				PackageFacet.parse(json.getObject("package")),
				ItemReference.parse(json.getObject("parentReference")),
				RemoteItemFacet.parse(json.getObject("remoteItem")),
				SearchResultFacet.parse(json.getObject("searchResult")),
				SharedFacet.parse(json.getObject("shared")),
				SharePointIdsFacet.parse(json.getObject("sharepointIds")),
				json.getLong("size"),
				json.getString("webDavUrl"),
				json.getString("webUrl")
		);
	}
}
