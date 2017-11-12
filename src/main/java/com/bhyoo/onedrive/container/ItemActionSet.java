package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.container.action.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

public class ItemActionSet {
	@Getter @Setter(PRIVATE) protected @Nullable CommentAction comment;
	@Getter @Setter(PRIVATE) protected @Nullable Object create;
	@Getter @Setter(PRIVATE) protected @Nullable DeleteAction delete;
	@Getter @Setter(PRIVATE) protected @Nullable Object edit;
	@Getter @Setter(PRIVATE) protected @Nullable MentionAction mention;
	@Getter @Setter(PRIVATE) protected @Nullable MoveAction move;
	@Getter @Setter(PRIVATE) protected @Nullable RenameAction rename;
	@Getter @Setter(PRIVATE) protected @Nullable Object restore;
	@Getter @Setter(PRIVATE) protected @Nullable ShareAction share;
	@Getter @Setter(PRIVATE) protected @Nullable VersionAction version;
}
