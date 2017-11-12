package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.container.items.AbstractDriveItem;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

public class ItemActivity {
	@Getter @Setter(PRIVATE) protected @NotNull String id;
	@Getter @Setter(PRIVATE) protected @NotNull ItemActionSet action;
	@Getter @Setter(PRIVATE) protected @NotNull IdentitySet actor;
	@Getter @Setter(PRIVATE) protected @NotNull AbstractDriveItem driveItem;
	// TODO: @Getter @Setter(PRIVATE) protected @NotNull ListItem listItem;
	@Getter @Setter(PRIVATE) protected @NotNull ItemActivityTimeSet times;


	protected static class ItemActivityTimeSet {
		public @NotNull String observedDateTime, recordedDateTime;
	}
}
