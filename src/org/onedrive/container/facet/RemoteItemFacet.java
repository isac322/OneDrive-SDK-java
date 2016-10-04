package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.onedrive.container.items.ItemReference;

/**
 * https://dev.onedrive.com/facets/remoteitem_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class RemoteItemFacet {
	@Getter protected final String id;
	@Getter protected final ItemReference parentReference;
	@Getter protected final FolderFacet folder;
	@Getter protected final FileFacet file;
	@Getter protected final FileSystemInfoFacet fileSystemInfo;
	@Getter protected final long size;
	@Getter protected final String name;

	protected RemoteItemFacet(String id, ItemReference parentReference, FolderFacet folder, FileFacet file,
							  FileSystemInfoFacet fileSystemInfo, long size, String name) {
		this.id = id;
		this.parentReference = parentReference;
		this.folder = folder;
		this.file = file;
		this.fileSystemInfo = fileSystemInfo;
		this.size = size;
		this.name = name;
	}

	@Nullable
	public static RemoteItemFacet parse(JSONObject json) {
		if (json == null) return null;

		return new RemoteItemFacet(
				json.getString("id"),
				ItemReference.parse(json.getObject("parentReference")),
				FolderFacet.parse(json.getObject("folder")),
				FileFacet.parse(json.getObject("file")),
				FileSystemInfoFacet.parse(json.getObject("fileSystemInfo")),
				json.getLong("size"),
				json.getString("name")
		);
	}
}
