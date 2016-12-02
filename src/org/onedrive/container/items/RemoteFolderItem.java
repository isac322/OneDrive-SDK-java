package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.onedrive.Client;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.exceptions.ErrorResponseException;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = RemoteFolderItem.class)
public class RemoteFolderItem extends FolderItem {
	@Getter @NotNull protected RemoteItemFacet remoteItem;

	@JsonCreator
	protected RemoteFolderItem(@JacksonInject("OneDriveClient") Client client,
							   @JsonProperty("id") @NotNull String id,
							   @JsonProperty("createdBy") IdentitySet createdBy,
							   @JsonProperty("createdDateTime") String createdDateTime,
							   @JsonProperty("cTag") String cTag,
							   @JsonProperty("deleted") ObjectNode deleted,
							   @JsonProperty("description") String description,
							   @JsonProperty("eTag") String eTag,
							   @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
							   @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
							   @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
							   @JsonProperty("name") @NotNull String name,
							   @JsonProperty("parentReference") ItemReference parentReference,
							   @JsonProperty("remoteItem") @NotNull RemoteItemFacet remoteItem,
							   @JsonProperty("root") ObjectNode root,
							   @JsonProperty("searchResult") SearchResultFacet searchResult,
							   @JsonProperty("shared") SharedFacet shared,
							   @JsonProperty("sharePointIds") SharePointIdsFacet sharePointIds,
							   @JsonProperty("size") Long size,
							   @JsonProperty("specialFolder") SpecialFolderFacet specialFolder,
							   @JsonProperty("webDavUrl") String webDavUrl,
							   @JsonProperty("webUrl") String webUrl,
							   @JsonProperty("children@odata.nextLink") String nextLink,
							   @JsonProperty("children") ArrayNode children) {
		super(client, id, createdBy, createdDateTime, cTag, deleted, description, eTag, fileSystemInfo,
				remoteItem.getFolder(), lastModifiedBy, lastModifiedDateTime, name, parentReference, root,
				searchResult, shared, sharePointIds, remoteItem.getSize(), specialFolder, webDavUrl, webUrl, nextLink,
				children);

		this.remoteItem = remoteItem;
	}

	@NotNull
	@JsonIgnore
	public String getRealDriveID() {
		return remoteItem.getParentReference().driveId;
	}

	@NotNull
	@JsonIgnore
	public String getRealID() {
		return remoteItem.getId();
	}

	@Override
	protected void fetchChildren() throws ErrorResponseException {
		// children of remote folder can be obtained only by real drive's id and real id.
		_fetchChildren(String.format("/drives/%s/items/%s/children", getRealDriveID(), getRealID()));
	}
}
