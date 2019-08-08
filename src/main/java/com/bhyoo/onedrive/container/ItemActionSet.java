package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.container.action.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

public class ItemActionSet {
	@Getter protected final @Nullable CommentAction comment;
	@Getter protected final boolean create;
	@Getter protected final @Nullable DeleteAction delete;
	@Getter protected final boolean edit;
	@Getter protected final @Nullable MentionAction mention;
	@Getter protected final @Nullable MoveAction move;
	@Getter protected final @Nullable RenameAction rename;
	@Getter protected final boolean restore;
	@Getter protected final @Nullable ShareAction share;
	@Getter protected final @Nullable VersionAction version;

	protected ItemActionSet(@Nullable CommentAction comment, boolean create, @Nullable DeleteAction delete,
							boolean edit, @Nullable MentionAction mention, @Nullable MoveAction move,
							@Nullable RenameAction rename, boolean restore, @Nullable ShareAction share,
							@Nullable VersionAction version) {
		this.comment = comment;
		this.create = create;
		this.delete = delete;
		this.edit = edit;
		this.mention = mention;
		this.move = move;
		this.rename = rename;
		this.restore = restore;
		this.share = share;
		this.version = version;
	}


	public static ItemActionSet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable CommentAction comment = null;
		boolean create = false;
		@Nullable DeleteAction delete = null;
		boolean edit = false;
		@Nullable MentionAction mention = null;
		@Nullable MoveAction move = null;
		@Nullable RenameAction rename = null;
		boolean restore = false;
		@Nullable ShareAction share = null;
		@Nullable VersionAction version = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "comment":
					comment = CommentAction.deserialize(parser);
					break;
				case "create":
					create = true;
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						// TODO
					}
					break;
				case "delete":
					delete = DeleteAction.deserialize(parser);
					break;
				case "edit":
					edit = true;
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						// TODO
					}
					break;
				case "mention":
					mention = MentionAction.deserialize(parser);
					break;
				case "move":
					move = MoveAction.deserialize(parser);
					break;
				case "rename":
					rename = RenameAction.deserialize(parser);
					break;
				case "restore":
					restore = true;
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						// TODO
					}
					break;
				case "share":
					share = ShareAction.deserialize(parser);
					break;
				case "version":
					version = VersionAction.deserialize(parser);
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in ItemActionSet : " + currentName);
			}
		}

		return new ItemActionSet(comment, create, delete, edit, mention, move, rename, restore, share, version);
	}
}
