package com.bhyoo.onedrive.container.action;

import com.bhyoo.onedrive.container.IdentitySet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CommentAction {
	@Getter protected final boolean isReply;
	@Getter protected final @NotNull IdentitySet parentAuthor;
	@Getter protected final @NotNull IdentitySet participants;

	protected CommentAction(boolean isReply, @NotNull IdentitySet parentAuthor, @NotNull IdentitySet participants) {
		this.isReply = isReply;
		this.parentAuthor = parentAuthor;
		this.participants = participants;
	}


	@SuppressWarnings("ConstantConditions")
	public static @NotNull CommentAction deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull Boolean isReply = null;
		@NotNull IdentitySet parentAuthor = null;
		@NotNull IdentitySet participants = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "isReply":
					isReply = parser.getBooleanValue();
					break;
				case "parentAuthor":
					parentAuthor = IdentitySet.deserialize(parser);
					break;
				case "participants":
					participants = IdentitySet.deserialize(parser);
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in CommentAction : " + currentName);
			}
		}

		return new CommentAction(isReply, parentAuthor, participants);
	}
}

