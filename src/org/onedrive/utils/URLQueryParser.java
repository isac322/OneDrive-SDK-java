package org.onedrive.utils;

import java.net.URI;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * {@// TODO: Enhance javadoc}
 * original: http://stackoverflow.com/a/13592567
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class URLQueryParser {
	public static Map<String, List<String>> splitQuery(URI url) {
		return Arrays.stream(url.getQuery().split("&"))
				.map(URLQueryParser::splitQueryParameter)
				.collect(Collectors.groupingBy(
						SimpleImmutableEntry::getKey,
						HashMap<String, List<String>>::new,
						mapping(Map.Entry::getValue, toList())));
	}

	public static SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
		final int idx = it.indexOf("=");
		final String key = idx > 0 ? it.substring(0, idx) : it;
		final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
		return new AbstractMap.SimpleImmutableEntry<>(key, value);
	}
}
