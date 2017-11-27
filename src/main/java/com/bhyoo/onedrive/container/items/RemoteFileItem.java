package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.facet.*;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.DownloadFuture;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class RemoteFileItem extends AbstractRemoteItem implements FileItem {
	RemoteFileItem(@NotNull String id, @NotNull String createdDateTime, @Nullable String description,
				   @NotNull String eTag, @NotNull String lastModifiedDateTime, @NotNull String name,
				   @NotNull URI webUrl, @NotNull Client client, @NotNull String cTag, @Nullable ObjectNode deleted,
				   @NotNull ItemReference parentReference, @Nullable SearchResultFacet searchResult,
				   @Nullable SharedFacet shared, @Nullable SharePointIdsFacet sharePointIds, URI webDavUrl,
				   @NotNull RemoteItemFacet remoteItem) {
		super(id, createdDateTime, description, eTag, lastModifiedDateTime, name, webUrl, client, cTag, deleted,
				parentReference, searchResult, shared, sharePointIds, webDavUrl, remoteItem);
	}

	@Override
	public void download(@NotNull String path) throws IOException, ErrorResponseException {
		client.download(idPointer, Paths.get(path), this.name);
	}

	@Override
	public void download(@NotNull String path, @NotNull String newName) throws IOException, ErrorResponseException {
		client.download(idPointer, Paths.get(path), newName);
	}

	@Override
	public void download(@NotNull Path folderPath) throws IOException, ErrorResponseException {
		client.download(idPointer, folderPath, this.name);
	}

	// TODO: handling overwriting file

	@Override
	public void download(@NotNull Path folderPath, String newName) throws IOException, ErrorResponseException {
		client.download(idPointer, folderPath, newName);
	}


	@Override
	public @NotNull DownloadFuture downloadAsync(@NotNull Path folderPath) throws IOException {
		return client.downloadAsync(idPointer, folderPath, this.name);
	}

	@Override
	public @NotNull DownloadFuture downloadAsync(@NotNull Path folderPath, String newName) throws IOException {
		return client.downloadAsync(idPointer, folderPath, newName);
	}


	@Override public @Nullable String getMimeType() {return remoteItem.getFile().getMimeType();}

	@Override public @Nullable String getCRC32() {return remoteItem.getFile().getCrc32Hash();}

	@Override public @Nullable String getSHA1() {return remoteItem.getFile().getSha1Hash();}

	@Override public @Nullable String getQuickXorHash() {return remoteItem.getFile().getQuickXorHash(); }

	@Override public @Nullable AudioFacet getAudio() {return null;}

	@Override public @Nullable ImageFacet getImage() {return null;}

	@Override public @Nullable LocationFacet getLocation() {return null;}

	@Override public @Nullable PhotoFacet getPhoto() {return null;}

	@Override public @Nullable VideoFacet getVideo() {return null;}
}
