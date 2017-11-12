package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.IdentitySet;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public interface BaseItem {
	@NotNull String getId();

	@NotNull IdentitySet getCreator();

	@NotNull String getCreatedDateTime();

	@NotNull String getDescription();

	@NotNull String getETag();

	@NotNull IdentitySet getLastModifier();

	@NotNull String getLastModifiedDateTime();

	@NotNull String getName();

	@NotNull URI getWebUrl();
}
