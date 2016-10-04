package org.onedrive.container.items;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.network.BadRequestException;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.utils.OneDriveRequest;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/resources/item.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
abstract public class BaseItem extends BaseContainer {
	@Getter protected String id;
	@Getter protected IdentitySet createdBy;
	@Getter protected ZonedDateTime createdDateTime;
	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter protected String cTag;
	protected boolean deleted;
	@Getter protected String description;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter protected String eTag;
	@Getter protected FileSystemInfoFacet fileSystemInfo;
	@Getter protected IdentitySet lastModifiedBy;
	@Getter protected ZonedDateTime lastModifiedDateTime;
	@Getter protected String name;
	@Getter protected ItemReference parentReference;
	@Getter @Nullable protected RemoteItemFacet remoteItem;
	@Getter @Nullable protected SearchResultFacet searchResult;
	@Getter protected SharedFacet shared;
	@Getter @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter protected long size;
	@Getter protected String webDavUrl;
	@Getter protected String webUrl;
	protected Client client;

	@NotNull
	public static BaseItem parse(Client client, JSONObject json) {
		if (json.containsKey("file")) {
			return FileItem.parseFile(client, json);
		}
		else if (json.containsKey("folder")) {
			return FolderItem.parseFolder(client, json);
		}
		else if (json.containsKey("package")) {
			// TODO: Handling Package (https://dev.onedrive.com/facets/package_facet.htm).
			return PackageItem.parsePackage(client, json);
		}
		else {
			throw new UnsupportedOperationException("Item object neither file nor folder.");
		}
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isRemote() {
		return remoteItem != null;
	}

	public void delete() throws IOException {
		HttpsResponse response = OneDriveRequest.doDelete("/drive/items/" + id, client.getAccessToken());

		if (response.getCode() != 200) {
			throw new BadRequestException("Bad request. It must be already deleted item or wrong ID.");
		}
	}
}
