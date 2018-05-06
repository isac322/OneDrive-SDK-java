package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.DriveType;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

// TODO: is there any way to merge with {@link BasePointer}? cause it's conflict in behavior

/**
 * <a href="https://dev.onedrive.com/resources/itemReference.htm">https://dev.onedrive.com/resources/itemReference
 * .htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@ToString
@EqualsAndHashCode(of = {"id", "driveId"})
public class ItemReference {
	@Getter protected @NotNull String driveId;
	@Getter protected @NotNull DriveType driveType;
	/**
	 * only null when root directory
 	 */
	@Getter protected @Nullable String id;
	/**
	 * only null on Business version
	 */
	@Getter protected @Nullable String name;
	/**
	 * only null when root directory
	 */
	@Getter protected @Nullable PathPointer pathPointer;
	/**
	 * only null when root directory
	 */
	@Getter protected @Nullable String rawPath;
	@Getter protected @Nullable String shareId;
	@Getter protected @Nullable SharePointIdsFacet sharepointIds;

	protected ItemReference(@NotNull String driveId, @NotNull DriveType driveType, @Nullable String id,
							@Nullable String name, @Nullable String rawPath, @Nullable String shareId,
							@Nullable SharePointIdsFacet sharepointIds) {
		this.driveId = driveId;
		this.driveType = driveType;
		this.id = id;
		this.name = name;
		this.rawPath = rawPath;
		this.shareId = shareId;
		this.sharepointIds = sharepointIds;

		if (rawPath != null) {
			this.pathPointer = new PathPointer(rawPath, driveId);
		}
	}

	ItemReference(@NotNull String driveId, @NotNull DriveType driveType,
				  @Nullable String id, @Nullable PathPointer pathPointer) {
		this.driveId = driveId;
		this.driveType = driveType;
		this.id = id;
		this.pathPointer = pathPointer;

		if (pathPointer != null)
			this.rawPath = pathPointer.toASCIIApi();
		else
			this.rawPath = null;
	}

	public static ItemReference deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String driveId = null;
		@Nullable DriveType driveType = null;
		@Nullable String id = null;
		@Nullable String name = null;
		@Nullable String rawPath = null;
		@Nullable String shareId = null;
		@Nullable SharePointIdsFacet sharepointIds = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "driveId":
					driveId = parser.getText();
					break;
				case "driveType":
					driveType = DriveType.deserialize(parser.getText());
					break;
				case "id":
					id = parser.getText();
					break;
				case "name":
					name = parser.getText();
					break;
				case "path":
					rawPath = parser.getText();
					break;
				case "shareId":
					shareId = parser.getText();
					break;
				case "sharepointIds":
					sharepointIds = SharePointIdsFacet.deserialize(parser);
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in ItemReference : " + currentName);
			}
		}

		assert driveId != null : "driveId is null";
		assert driveType != null : "driveType is null";

		return new ItemReference(driveId, driveType, id, name, rawPath, shareId, sharepointIds);
	}
}
