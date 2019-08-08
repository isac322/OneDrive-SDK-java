package com.bhyoo.onedrive.container.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class DeleteAction {
	@Getter protected final @NotNull String name;

	protected DeleteAction(@NotNull String name) {this.name = name;}

	@SuppressWarnings("ConstantConditions")
	public static @NotNull DeleteAction deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull String name = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "name":
					name = parser.getText();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in DeleteAction : " + currentName);
			}
		}

		return new DeleteAction(name);
	}
}
