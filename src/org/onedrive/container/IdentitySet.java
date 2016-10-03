package org.onedrive.container;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class IdentitySet {
	@Getter @Nullable protected final Identity user;
	@Getter @Nullable protected final Identity application;
	@Getter @Nullable protected final Identity device;
	@Getter @Nullable protected final Identity organization;

	protected IdentitySet(Identity user, Identity app, Identity device, Identity org) {
		this.user = user;
		this.application = app;
		this.device = device;
		this.organization = org;
	}

	@Nullable
	public static IdentitySet parse(JSONObject json) {
		if (json == null) return null;

		return new IdentitySet(
				Identity.parse(json.getObject("user")),
				Identity.parse(json.getObject("application")),
				Identity.parse(json.getObject("device")),
				Identity.parse(json.getObject("organization")));
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		if (user != null) buffer.append("user : { ").append(user).append(" }, ");
		if (application != null) buffer.append("application : { ").append(application).append(" }, ");
		if (device != null) buffer.append("device : { ").append(device).append("}, ");
		if (organization != null) buffer.append("organization : { ").append(organization).append(" }, ");

		return buffer.toString();
	}
}
