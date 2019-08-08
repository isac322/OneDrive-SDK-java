package com.bhyoo.onedrive.container.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class VersionAction {
	@Getter protected final @NotNull String newVersion;

	protected VersionAction(@NotNull String newVersion) {this.newVersion = newVersion;}

	@SuppressWarnings("ConstantConditions")
	public static @NotNull VersionAction deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull String newVersion = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "newVersion":
					newVersion = parser.getText();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in VersionAction : " + currentName);
			}
		}

		return new VersionAction(newVersion);
	}
}
