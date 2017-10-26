package com.bhyoo.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ResponsePage<T> {
	@Getter @Setter(PRIVATE) @JsonProperty("@odata.nextLink") private String nextLink;
	@Getter @Setter(PRIVATE) private T[] value;
}
