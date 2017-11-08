package com.bhyoo.onedrive.container;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode
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
