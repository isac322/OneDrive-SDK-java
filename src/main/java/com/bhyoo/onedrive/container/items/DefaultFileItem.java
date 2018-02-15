package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.facet.*;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.DownloadFuture;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class DefaultFileItem extends AbstractDriveItem implements FileItem {
	@Getter(onMethod = @__(@Override)) protected @Nullable AudioFacet audio;
	protected @NotNull FileFacet file;
	@Getter(onMethod = @__(@Override)) protected @Nullable ImageFacet image;
	@Getter(onMethod = @__(@Override)) protected @Nullable LocationFacet location;
	@Getter(onMethod = @__(@Override)) protected @Nullable PhotoFacet photo;
	@Getter(onMethod = @__(@Override)) protected @Nullable VideoFacet video;


	DefaultFileItem(@NotNull String id, @NotNull IdentitySet creator, @NotNull String createdDateTime,
					@Nullable String description, @NotNull String eTag, @NotNull IdentitySet lastModifier,
					@NotNull String lastModifiedDateTime, @NotNull String name, @NotNull URI webUrl,
					@NotNull Client client, @NotNull String cTag, @Nullable ObjectNode deleted,
					FileSystemInfoFacet fileSystemInfo, @NotNull ItemReference parentReference,
					@Nullable SearchResultFacet searchResult, @Nullable SharedFacet shared,
					@Nullable SharePointIdsFacet sharePointIds, @NotNull Long size, URI webDavUrl,
					@Nullable AudioFacet audio, @NotNull FileFacet file, @Nullable ImageFacet image,
					@Nullable LocationFacet location, @Nullable PhotoFacet photo, @Nullable VideoFacet video) {
		super(id, creator, createdDateTime, description, eTag, lastModifier, lastModifiedDateTime, name, webUrl,
				client, cTag, deleted, fileSystemInfo, parentReference, searchResult, shared, sharePointIds, size,
				webDavUrl);
		this.audio = audio;
		this.file = file;
		this.image = image;
		this.location = location;
		this.photo = photo;
		this.video = video;

		createPointers();
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


	@Override
	protected void refreshBy(@NotNull AbstractDriveItem newItem) {
		super.refreshBy(newItem);

		DefaultFileItem item = (DefaultFileItem) newItem;

		this.audio = item.audio;
		this.file = item.file;
		this.image = item.image;
		this.location = item.location;
		this.photo = item.photo;
		this.video = item.video;
	}



	/*
	 *************************************************************
	 *
	 * Custom Getter
	 *
	 *************************************************************
	 */


	@Override
	public @Nullable String getMimeType() {return this.file.getMimeType();}

	@Override
	public @Nullable String getCRC32() {return this.file.getCrc32Hash();}

	@Override
	public @Nullable String getSHA1() {return this.file.getSha1Hash();}

	@Override
	public @Nullable String getQuickXorHash() {return this.file.getQuickXorHash();}
}
