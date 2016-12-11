package org.onedrive.container;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class IdentitySet {
	@Getter @Setter(PRIVATE) @Nullable protected Identity user;
	@Getter @Setter(PRIVATE) @Nullable protected Identity application;
	@Getter @Setter(PRIVATE) @Nullable protected Identity device;
	@Getter @Setter(PRIVATE) @Nullable protected Identity organization;

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
