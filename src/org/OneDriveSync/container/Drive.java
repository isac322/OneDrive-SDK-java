package org.OneDriveSync.container;

import lombok.Getter;
import org.simpler.json.JsonObject;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
 */
public class Drive extends Container {
	@Getter protected final String id;
	@Getter protected final String driveType;
	@Getter protected final String state;
	@Getter protected final IdentitySet identitySet;
	@Getter protected final long totalCapacity;
	@Getter protected final long deleted;
	@Getter protected final long usedCapacity;
	@Getter protected final long remaining;

	protected Drive(String id, String driveType, IdentitySet identitySet, String state, long totalCapacity,
					long deleted, long usedCapacity, long remaining) {
		this.id = id;
		this.driveType = driveType;
		this.state = state;
		this.identitySet = identitySet;
		this.totalCapacity = totalCapacity;
		this.deleted = deleted;
		this.usedCapacity = usedCapacity;
		this.remaining = remaining;
	}

	public static Drive parse(JsonObject json) {
		JsonObject quota = json.getObject("quota");

		String id = json.getString("id");

		if (Drive.contains(id)) {
			return (Drive) Drive.get(id);

		} else {
			Drive drive = new Drive(
					json.getString("id"),
					json.getString("driveType"),
					IdentitySet.parse(json.getObject("owner")),
					quota.getString("state"),
					quota.getLong("total"),
					quota.getLong("deleted"),
					quota.getLong("used"),
					quota.getLong("remaining")
			);

			Drive.put(drive);

			return drive;
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Drive && id.equals(((Drive) obj).getId());
	}

	@Override
	public String toString() {
		return id + ' ' + driveType + ' ' + state + ' ' + identitySet + ' ' + totalCapacity + ' ' + deleted + ' ' + usedCapacity + ' ' + remaining;
	}
}
