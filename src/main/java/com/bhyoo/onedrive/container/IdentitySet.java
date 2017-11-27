package com.bhyoo.onedrive.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public class IdentitySet {
	@Getter protected @Nullable Identity user;
	@Getter protected @Nullable Identity application;
	@Getter protected @Nullable Identity device;
	@Getter protected @Nullable Identity organization;
	@Getter protected @NotNull Identity[] extraIdentity;

	protected IdentitySet(@Nullable Identity user, @Nullable Identity application,
						  @Nullable Identity device, @Nullable Identity organization,
						  @NotNull Identity[] extraIdentity) {
		this.user = user;
		this.application = application;
		this.device = device;
		this.organization = organization;
		this.extraIdentity = extraIdentity;
	}

	public static IdentitySet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable Identity user = null;
		@Nullable Identity application = null;
		@Nullable Identity device = null;
		@Nullable Identity organization = null;
		ArrayList<Identity> extraIdentities = new ArrayList<>();

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "user":
					user = Identity.deserialize(parser);
					break;
				case "application":
					application = Identity.deserialize(parser);
					break;
				case "device":
					device = Identity.deserialize(parser);
					break;
				case "organization":
					organization = Identity.deserialize(parser);
					break;
				default:
					try {
						Identity extra = Identity.deserialize(parser);
						extraIdentities.add(extra);
						break;
					}
					catch (IOException e) {
						throw new IllegalStateException("Unknown attribute detected in IdentitySet : " + currentName);
					}
			}
		}

		return new IdentitySet(user, application, device, organization, extraIdentities.toArray(new Identity[0]));
	}
}
