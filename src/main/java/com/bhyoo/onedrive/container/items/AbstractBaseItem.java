package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.IdentitySet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = "id")
@ToString(doNotUseGetters = true)
abstract public class AbstractBaseItem implements BaseItem {
	@Getter(onMethod = @__(@Override)) protected @NotNull String id;

	// createdBy
	@Getter(onMethod = @__(@Override)) protected @Nullable IdentitySet creator;

	// TODO: convert datetime to some appreciate object
	@Getter(onMethod = @__(@Override)) protected @Nullable String createdDateTime;
	@Getter(onMethod = @__(@Override)) protected @Nullable String description;

	// eTag
	@Getter(onMethod = @__(@Override)) protected @Nullable String eTag;

	// lastModifiedBy
	@Getter(onMethod = @__(@Override)) protected @Nullable IdentitySet lastModifier;

	// TODO: convert datetime to some appreciate object
	@Getter(onMethod = @__(@Override)) protected @Nullable String lastModifiedDateTime;
	@Getter(onMethod = @__(@Override)) protected @Nullable String name;
	@Getter(onMethod = @__(@Override)) protected @Nullable URI webUrl;

	AbstractBaseItem(@NotNull String id, @Nullable IdentitySet creator, @Nullable String createdDateTime,
					 @Nullable String description, @Nullable String eTag, @Nullable IdentitySet lastModifier,
					 @Nullable String lastModifiedDateTime, @Nullable String name, @Nullable URI webUrl) {
		this.id = id;
		this.creator = creator;
		this.createdDateTime = createdDateTime;
		this.description = description;
		this.eTag = eTag;
		this.lastModifier = lastModifier;
		this.lastModifiedDateTime = lastModifiedDateTime;
		this.name = name;
		this.webUrl = webUrl;
	}
}
