package com.bhyoo.onedrive.container.action;

import com.bhyoo.onedrive.container.IdentitySet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class MentionAction {
	@Getter protected final @NotNull IdentitySet mentionees;

	protected MentionAction(@NotNull IdentitySet mentionees) {this.mentionees = mentionees;}

	@SuppressWarnings("ConstantConditions")
	public static @NotNull MentionAction deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull IdentitySet mentionees = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "mentionees":
					mentionees = IdentitySet.deserialize(parser);
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in MentionAction : " + currentName);
			}
		}

		return new MentionAction(mentionees);
	}
}
