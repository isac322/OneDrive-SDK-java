package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.items.AbstractDriveItem;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ItemActivity {
	@Getter protected final @NotNull String id;
	@Getter protected final @NotNull ItemActionSet action;
	@Getter protected final @NotNull IdentitySet actor;
	@Getter protected final @NotNull AbstractDriveItem driveItem;
	// TODO: @Getter @Setter(PRIVATE) protected @NotNull ListItem listItem;
	@Getter protected final @NotNull ItemActivityTimeSet times;

	protected ItemActivity(@NotNull String id, @NotNull ItemActionSet action, @NotNull IdentitySet actor,
						   @NotNull AbstractDriveItem driveItem, @NotNull ItemActivityTimeSet times) {
		this.id = id;
		this.action = action;
		this.actor = actor;
		this.driveItem = driveItem;
		this.times = times;
	}


	@SuppressWarnings("ConstantConditions")
	public static ItemActivity deserialize(@NotNull Client client, @NotNull JsonParser parser) throws IOException {
		@NotNull String id = null;
		@NotNull ItemActionSet action = null;
		@NotNull IdentitySet actor = null;
		@NotNull AbstractDriveItem driveItem = null;
		@NotNull ItemActivityTimeSet times = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "id":
					id = parser.getText();
					break;
				case "action":
					action = ItemActionSet.deserialize(parser);
					break;
				case "actor":
					actor = IdentitySet.deserialize(parser);
					break;
				case "driveItem":
					driveItem = AbstractDriveItem.deserialize(client, parser, false);
					break;
				case "times":
					times = ItemActivityTimeSet.deserialize(parser);
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in ItemActivity : " + currentName);
			}
		}

		return new ItemActivity(id, action, actor, driveItem, times);
	}

	protected static class ItemActivityTimeSet {
		public final @NotNull String observedDateTime, recordedDateTime;

		protected ItemActivityTimeSet(@NotNull String observedDateTime, @NotNull String recordedDateTime) {
			this.observedDateTime = observedDateTime;
			this.recordedDateTime = recordedDateTime;
		}

		@SuppressWarnings("ConstantConditions")
		public static ItemActivityTimeSet deserialize(@NotNull JsonParser parser) throws IOException {
			@NotNull String observedDateTime = null;
			@NotNull String recordedDateTime = null;

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String currentName = parser.getCurrentName();
				parser.nextToken();

				switch (currentName) {
					case "observedDateTime":
						observedDateTime = parser.getText();
						break;
					case "recordedDateTime":
						recordedDateTime = parser.getText();
						break;
					default:
						throw new IllegalStateException(
								"Unknown attribute detected in ItemActivityTimeSet : " + currentName);
				}
			}

			return new ItemActivityTimeSet(observedDateTime, recordedDateTime);
		}
	}
}
