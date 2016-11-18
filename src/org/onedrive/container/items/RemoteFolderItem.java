package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.onedrive.Client;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.network.legacy.HttpsRequest;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@// TODO: enhance javadoc}
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

		if (size != null) {
			throw new RuntimeException("DEV: Size facet is exist in RemoteItem!!!!");
		}
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
	protected void fetchChildren() {
		ObjectNode content = client.getRequestTool().doGetJson(
				String.format("/drives/%s/items/%s/children", getRealDriveID(), getRealID()));

		allChildren = new CopyOnWriteArrayList<>();
		folderChildren = new CopyOnWriteArrayList<>();
		fileChildren = new CopyOnWriteArrayList<>();

		JsonNode value = content.get("value");
		JsonNode nextLink = content.get("@odata.nextLink");

		if (!value.isArray() || (nextLink != null && !nextLink.isTextual())) {
			// TODO: custom exception
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}

		// TODO: if-none-match request header handling.
		// TODO: not 200 OK response handling.
		parseChildren(client, value, nextLink == null ? null : nextLink.asText(),
				allChildren, folderChildren, fileChildren);
	}
}
