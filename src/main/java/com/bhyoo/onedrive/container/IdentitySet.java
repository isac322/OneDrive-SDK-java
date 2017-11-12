package com.bhyoo.onedrive.container;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public class IdentitySet {
	@Getter @Setter(PRIVATE) protected @Nullable Identity user;
	@Getter @Setter(PRIVATE) protected @Nullable Identity application;
	@Getter @Setter(PRIVATE) protected @Nullable Identity device;
	@Getter @Setter(PRIVATE) protected @Nullable Identity organization;
}
