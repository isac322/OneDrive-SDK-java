package org.OneDriveSync.container;

import lombok.Getter;
import org.simpler.json.JsonObject;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 2.
 */
public class Identity extends Container {
	@Getter protected final String id;
	@Getter protected final String displayName;
	@Getter protected final JsonObject thumbnails;

	protected Identity(String name, String id, JsonObject thumbnails) {
		this.displayName = name;
		this.id = id;
		this.thumbnails = thumbnails;
	}

	public static Identity parse(JsonObject json) {
		String id = json.getString("id");

		if (Identity.contains(id)) {
			return (Identity) Identity.get(id);

		} else {
			Identity identity = new Identity(
					json.getString("id"),
					json.getString("displayName"),
					json.getObject("thumbnails")
			);

			Identity.put(identity);

			return identity;
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Identity && id.equals(((Identity) obj).getId());
	}

	@Override
	public String toString() {
		return displayName + ' ' + id;
	}
}
