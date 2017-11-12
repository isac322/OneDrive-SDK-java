package com.bhyoo.onedrive.container.action;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: add pointer (PathPointer)
public class MoveAction {
	@Getter @Setter(PRIVATE) protected @NotNull String from;
	@Getter @Setter(PRIVATE) protected @NotNull String to;
}
