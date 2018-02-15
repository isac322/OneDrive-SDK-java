package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.container.action.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ItemActionSet {
	@Getter protected final @Nullable CommentAction comment;
	@Getter protected final @Nullable ObjectNode create;
	@Getter protected final @Nullable DeleteAction delete;
	@Getter protected final @Nullable Object edit;
	@Getter protected final @Nullable MentionAction mention;
	@Getter protected final @Nullable MoveAction move;
	@Getter protected final @Nullable RenameAction rename;
	@Getter protected final @Nullable Object restore;
	@Getter protected final @Nullable ShareAction share;
	@Getter protected final @Nullable VersionAction version;

	protected ItemActionSet(@Nullable CommentAction comment, @Nullable ObjectNode create,
							@Nullable DeleteAction delete, @Nullable ObjectNode edit, @Nullable MentionAction mention,
							@Nullable MoveAction move, @Nullable RenameAction rename, @Nullable ObjectNode restore,
							@Nullable ShareAction share, @Nullable VersionAction version) {
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
		@Nullable ObjectNode create = null;
		@Nullable DeleteAction delete = null;
		@Nullable ObjectNode edit = null;
		@Nullable MentionAction mention = null;
		@Nullable MoveAction move = null;
		@Nullable RenameAction rename = null;
		@Nullable ObjectNode restore = null;
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
					create = parser.readValueAs(ObjectNode.class);
					break;
				case "delete":
					delete = DeleteAction.deserialize(parser);
					break;
				case "edit":
					edit = parser.readValueAs(ObjectNode.class);
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
					restore = parser.readValueAs(ObjectNode.class);
					break;
				case "share":
					share = ShareAction.deserialize(parser);
					break;
				case "version":
					version = VersionAction.deserialize(parser);
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in ItemActionSet : " + currentName);
			}
		}

		return new ItemActionSet(comment, create, delete, edit, mention, move, rename, restore, share, version);
	}
}
