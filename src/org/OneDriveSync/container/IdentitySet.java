package org.OneDriveSync.container;

import lombok.Getter;
import org.simpler.json.JsonObject;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * TODO: enhance javadoc
 * Created by isac322 on 16. 10. 3.
 */
public class IdentitySet {
	@Getter protected final Identity user;
	@Getter protected final Identity application;
	@Getter protected final Identity device;
	@Getter protected final Identity organization;

	protected IdentitySet(Identity user, Identity app, Identity device, Identity org) {
		this.user = user;
		this.application = app;
		this.device = device;
		this.organization = org;
	}

	public static IdentitySet parse(JsonObject json) {
		Identity user, app, device, org;
		user = app = device = org = null;

		if (json.contains("user")) user = Identity.parse(json.getObject("user"));
		if (json.contains("application")) app = Identity.parse(json.getObject("application"));
		if (json.contains("device")) device = Identity.parse(json.getObject("device"));
		if (json.contains("organization")) org = Identity.parse(json.getObject("organization"));

		return new IdentitySet(user, app, device, org);
	}
}
