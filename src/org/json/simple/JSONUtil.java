package org.json.simple;

import com.sun.istack.internal.Nullable;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
 *
 * @author isac322
 */
public class JSONUtil {
	@Nullable
	static JSONArray convertToJsonArray(Object value) {
		if (value == null) return null;
		else if (value instanceof JSONArray) return (JSONArray) value;
		else throw new ClassCastException("Invalid Cast");
	}

	@Nullable
	static JSONObject convertToJsonObject(Object value) {
		if (value == null) return null;
		else if (value instanceof JSONObject) return (JSONObject) value;
		else throw new ClassCastException("Invalid Cast");
	}

	@Nullable
	static String convertToString(Object value) {
		if (value == null) return null;
		else if (value instanceof String) return (String) value;
		else throw new ClassCastException("Invalid Cast");
	}

	@Nullable
	static Double convertToDouble(Object value) {
		if (value == null) return null;
		else if (value instanceof Double) return (Double) value;
		else throw new ClassCastException("Invalid Cast");
	}

	@Nullable
	static Long convertToLong(Object value) {
		if (value == null) return null;
		else if (value instanceof Long) return (Long) value;
		else throw new ClassCastException("Invalid Cast");
	}

	@Nullable
	static Boolean convertToBool(Object value) {
		if (value == null) return null;
		else if (value instanceof Boolean) return (Boolean) value;
		else throw new ClassCastException("Invalid Cast");
	}
}
