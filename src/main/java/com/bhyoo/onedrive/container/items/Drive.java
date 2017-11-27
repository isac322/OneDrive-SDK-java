package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.DriveType;
import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.ItemActivity;
import com.bhyoo.onedrive.container.facet.DriveState;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@ToString(doNotUseGetters = true)
public class Drive extends AbstractBaseItem {
	@Getter protected @Nullable ItemActivity[] activities;
	@Getter protected @NotNull DriveType driveType;
	@Getter protected @Nullable AbstractDriveItem[] items;
	@Getter protected @NotNull IdentitySet owner;
	protected @NotNull Quota quota;
	@Getter protected @Nullable AbstractDriveItem root;
	@Getter protected @Nullable AbstractDriveItem[] special;
	// TODO: custom class for this variable
	@Getter protected @Nullable ObjectNode system;


	protected Drive(@NotNull String id, @Nullable IdentitySet creator, @Nullable String createdDateTime,
					@Nullable String description, @Nullable String eTag, @Nullable IdentitySet lastModifier,
					@Nullable String lastModifiedDateTime, @Nullable String name, @Nullable URI webUrl,
					@Nullable ItemActivity[] activities, @NotNull DriveType driveType,
					@Nullable AbstractDriveItem[] items, @NotNull IdentitySet owner, @NotNull Quota quota,
					@Nullable AbstractDriveItem root, @Nullable AbstractDriveItem[] special,
					@Nullable ObjectNode system) {
		super(id, creator, createdDateTime, description, eTag, lastModifier, lastModifiedDateTime, name, webUrl);

		this.activities = activities;
		this.driveType = driveType;
		this.items = items;
		this.owner = owner;
		this.quota = quota;
		this.root = root;
		this.special = special;
		this.system = system;
	}

	@SneakyThrows(URISyntaxException.class)
	public static @NotNull Drive deserialize(@NotNull Client client, @NotNull JsonParser parser) throws IOException {
		@NotNull String id = null;
		@NotNull IdentitySet creator = null;
		@NotNull String createdDateTime = null;
		@NotNull String description = null;
		@NotNull String eTag = null;
		@NotNull IdentitySet lastModifier = null;
		@NotNull String lastModifiedDateTime = null;
		@NotNull String name = null;
		@NotNull URI webUrl = null;
		@Nullable ItemActivity[] activities = null;
		@NotNull DriveType driveType = null;
		@Nullable AbstractDriveItem[] items = null;
		@NotNull IdentitySet owner = null;
		@NotNull Quota quota = null;
		@Nullable AbstractDriveItem root = null;
		@Nullable AbstractDriveItem[] special = null;
		@Nullable ObjectNode system = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "id":
					id = parser.getText();
					break;
				case "createdBy":
					creator = IdentitySet.deserialize(parser);
					break;
				case "createdDateTime":
					createdDateTime = parser.getText();
					break;
				case "description":
					description = parser.getText();
					break;
				case "eTag":
					eTag = parser.getText();
					break;
				case "lastModifiedBy":
					lastModifier = IdentitySet.deserialize(parser);
					break;
				case "lastModifiedDateTime":
					lastModifiedDateTime = parser.getText();
					break;
				case "name":
					name = parser.getText();
					break;
				case "webUrl":
					webUrl = new URI(parser.getText());
					break;
				case "activities":
					ArrayList<ItemActivity> activityList = new ArrayList<>();
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						activityList.add(ItemActivity.deserialize(client, parser));
					}
					activities = activityList.toArray(new ItemActivity[0]);
					break;
				case "driveType":
					driveType = DriveType.deserialize(parser.getText());
					break;
				case "items":
					ArrayList<AbstractDriveItem> driveItems = new ArrayList<>();
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						driveItems.add(AbstractDriveItem.deserialize(client, parser, false));
					}
					items = driveItems.toArray(new AbstractDriveItem[0]);
					break;
				case "owner":
					owner = IdentitySet.deserialize(parser);
					break;
				case "quota":
					quota = Quota.deserialize(parser);
					break;
				case "root":
					root = AbstractDriveItem.deserialize(client, parser, false);
					break;
				case "special":
					ArrayList<AbstractDriveItem> specials = new ArrayList<>();
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						specials.add(AbstractDriveItem.deserialize(client, parser, false));
					}
					special = specials.toArray(new AbstractDriveItem[0]);
					break;
				case "system":
					system = parser.readValueAs(ObjectNode.class);
					break;
				case "@odata.context":
					// TODO
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in Drive : " + currentName);
			}
		}

		return new Drive(id, creator, createdDateTime, description, eTag, lastModifier, lastModifiedDateTime, name,
				webUrl, activities, driveType, items, owner, quota, root, special, system);
	}

	public long getTotalCapacity() {return quota.total;}

	public long getDeleted() {return quota.deleted;}

	public long getUsedCapacity() {return quota.used;}

	public long getRemaining() {return quota.remaining;}

	public DriveState getState() {return quota.state;}

	public DriveItem fetchRoot() {
		// TODO: implement
		return null;
	}


	@ToString
	static private class Quota {
		public DriveState state;
		public long total, deleted, used, remaining;

		public static Quota deserialize(@NotNull JsonParser parser) throws IOException {
			Quota quota = new Quota();

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String currentName = parser.getCurrentName();
				parser.nextToken();

				switch (currentName) {
					case "state":
						quota.state = DriveState.deserialize(parser.getText());
						break;
					case "total":
						quota.total = parser.getLongValue();
						break;
					case "deleted":
						quota.deleted = parser.getLongValue();
						break;
					case "used":
						quota.used = parser.getLongValue();
						break;
					case "remaining":
						quota.remaining = parser.getLongValue();
						break;
					default:
						throw new IllegalStateException("Unknown attribute detected in Quota : " + currentName);
				}
			}

			return quota;
		}
	}
}
