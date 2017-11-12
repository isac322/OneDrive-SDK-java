package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.IdentitySet;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = "id")
@ToString(doNotUseGetters = true)
abstract public class AbstractBaseItem implements BaseItem {
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull String id;

	@JsonProperty("createdBy")
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull IdentitySet creator;

	// TODO: convert datetime to some appreciate object
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull String createdDateTime;
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull String description;

	@JsonProperty("eTag")
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull String eTag;

	@JsonProperty("lastModifiedBy")
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull IdentitySet lastModifier;

	// TODO: convert datetime to some appreciate object
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull String lastModifiedDateTime;
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull String name;
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) protected @NotNull URI webUrl;
}
