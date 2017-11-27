package com.bhyoo.onedrive.container.action;

import com.bhyoo.onedrive.container.IdentitySet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ShareAction {
	@Getter protected final @NotNull IdentitySet recipients;

	protected ShareAction(@NotNull IdentitySet recipients) {this.recipients = recipients;}

	@SuppressWarnings("ConstantConditions")
	public static @NotNull ShareAction deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull IdentitySet recipients = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "recipients":
					recipients = IdentitySet.deserialize(parser);
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in ShareAction : " + currentName);
			}
		}

		return new ShareAction(recipients);
	}
}
