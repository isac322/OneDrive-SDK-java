package org.onedrive.container;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class IdentitySet {
	@Getter @Nullable protected final Identity user;
	@Getter @Nullable protected final Identity application;
	@Getter @Nullable protected final Identity device;
	@Getter @Nullable protected final Identity organization;

	@JsonCreator
	protected IdentitySet(@Nullable @JsonProperty("user") Identity user,
						  @Nullable @JsonProperty("application") Identity application,
						  @Nullable @JsonProperty("device") Identity device,
						  @Nullable @JsonProperty("organization") Identity organization) {
		this.user = user;
		this.application = application;
		this.device = device;
		this.organization = organization;
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
