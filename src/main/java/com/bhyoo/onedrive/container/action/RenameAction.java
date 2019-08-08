package com.bhyoo.onedrive.container.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class RenameAction {
	@Getter protected final @NotNull String oldName;

	protected RenameAction(@NotNull String oldName) {this.oldName = oldName;}

	@SuppressWarnings("ConstantConditions")
	public static @NotNull RenameAction deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull String oldName = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "oldName":
					oldName = parser.getText();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in RenameAction : " + currentName);
			}
		}

		return new RenameAction(oldName);
	}
}
