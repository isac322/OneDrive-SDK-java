package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.container.items.AbstractBaseItem;
import com.bhyoo.onedrive.container.items.AbstractDriveItem;
import com.bhyoo.onedrive.container.items.DriveItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: add javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@ToString(doNotUseGetters = true)
public class Drive extends AbstractBaseItem {
	@Getter @Setter(PRIVATE) protected @Nullable ItemActivity[] activities;
	@Getter @Setter(PRIVATE) protected @NotNull DriveType driveType;
	@Getter @Setter(PRIVATE) protected @Nullable AbstractDriveItem[] items;
	@Getter @Setter(PRIVATE) protected @NotNull IdentitySet owner;
	@JsonProperty @Setter(PRIVATE) protected @NotNull Quota quota;
	@Getter @Setter(PRIVATE) protected @Nullable AbstractDriveItem root;
	@Getter @Setter(PRIVATE) protected @Nullable AbstractDriveItem[] special;
	// TODO: custom class for this variable
	@Getter @Setter(PRIVATE) protected @Nullable ObjectNode system;


	@JsonIgnore public long getTotalCapacity() {return quota.total;}

	@JsonIgnore public long getDeleted() {return quota.deleted;}

	@JsonIgnore public long getUsedCapacity() {return quota.used;}

	@JsonIgnore public long getRemaining() {return quota.remaining;}

	@JsonIgnore public String getState() {return quota.state;}

	@JsonIgnore public DriveItem fetchRoot() {
		// TODO
		return null;
	}


	@ToString
	static private class Quota {
		public String state;
		public long total, deleted, used, remaining;
	}
}
