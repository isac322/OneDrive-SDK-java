package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.UploadFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface FolderItem extends DriveItem, Iterable<DriveItem> {
	// TODO: Implement '@name.conflictBehavior'

	// TODO: add more @throws

	/**
	 * Implementation of <a href='https://dev.onedrive.com/items/create.htm'>detail</a>.
	 * <p>
	 *
	 * @param name New folder name.
	 *
	 * @return New folder's object.
	 *
	 * @throws RuntimeException If creating folder or converting response is fails.
	 */
	@NotNull FolderItem createFolder(@NotNull String name) throws ErrorResponseException;

	@NotNull UploadFuture uploadFile(@NotNull Path filePath);



	/*
	 *************************************************************
	 *
	 * Custom Getter
	 *
	 *************************************************************
	 */

	boolean isDeleted();

	@Nullable String deletedState();

	boolean isRoot();

	boolean isChildrenFetched();

	void fetchChildren() throws ErrorResponseException;

	boolean isSpecial();

	long childCount();

	@NotNull DriveItem[] allChildren() throws ErrorResponseException;

	@NotNull FolderItem[] folderChildren() throws ErrorResponseException;

	@NotNull FileItem[] fileChildren() throws ErrorResponseException;
}
