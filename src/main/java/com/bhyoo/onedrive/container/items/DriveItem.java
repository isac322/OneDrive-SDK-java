package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.AsyncJobMonitor;
import com.bhyoo.onedrive.container.facet.FileSystemInfoFacet;
import com.bhyoo.onedrive.container.facet.SearchResultFacet;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.facet.SharedFacet;
import com.bhyoo.onedrive.container.items.pointer.BasePointer;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public interface DriveItem extends BaseItem {
	@Override String toString();


	/**
	 * This method refresh content even if you doesn't have changes.<br>
	 * <br>
	 * Note that when refresh content, <b>it can contains difference that you didn't modify</b>. because other side
	 * (it could be other App, other process, etc.) can modify content after you fetched.
	 *
	 * @throws ErrorResponseException if error happens while requesting copying operation. such as invalid login info
	 */
	void refresh() throws ErrorResponseException;




	/*
	 *************************************************************
	 *
	 * Deleting
	 *
	 * *************************************************************
	 */


	void delete() throws ErrorResponseException;




	/*
	 *************************************************************
	 *
	 * Coping
	 *
	 * *************************************************************
	 */


	@NotNull AsyncJobMonitor copyTo(@NotNull FolderItem folder) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull FolderItem folder, @NotNull String newName) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull ItemReference folder) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull ItemReference folder, @NotNull String newName) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull BasePointer dest) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull BasePointer dest, @NotNull String newName) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull String destId) throws ErrorResponseException;

	@NotNull AsyncJobMonitor copyTo(@NotNull String destId, @NotNull String newName) throws ErrorResponseException;




	/*
	 *************************************************************
	 *
	 * Moving
	 *
	 *************************************************************
	 */


	void moveTo(@NotNull FolderItem folder) throws ErrorResponseException;

	void moveTo(@NotNull ItemReference reference) throws ErrorResponseException;

	void moveTo(@NotNull String id) throws ErrorResponseException;

	void moveTo(@NotNull BasePointer pointer) throws ErrorResponseException;


	@NotNull ItemReference newReference();




	/*
	 *************************************************************
	 *
	 * Custom Getter
	 *
	 *************************************************************
	 */


	@NotNull String getDriveId();

	@NotNull String getCTag();

	@Nullable FileSystemInfoFacet getFileSystemInfo();

	@NotNull ItemReference getParentReference();

	@Nullable SearchResultFacet getSearchResult();

	@Nullable SharedFacet getShared();

	@Nullable SharePointIdsFacet getSharePointIds();

	@Nullable Long getSize();

	@Nullable URI getWebDavUrl();

	@Nullable PathPointer getPathPointer();

	@NotNull IdPointer getIdPointer();




	/*
	 *************************************************************
	 *
	 * Custom Setter
	 *
	 *************************************************************
	 */


	void updateDescription(String description) throws ErrorResponseException;

	void rename(@NotNull String name) throws ErrorResponseException;
}
