package com.bhyoo.onedrive.container.action;

import com.bhyoo.onedrive.container.IdentitySet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

public class CommentAction {
	@Getter @Setter(PRIVATE) protected boolean isReply;
	@Getter @Setter(PRIVATE) protected @NotNull IdentitySet parentAuthor;
	@Getter @Setter(PRIVATE) protected @NotNull IdentitySet participants;
}

