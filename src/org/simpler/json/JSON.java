package org.simpler.json;

import com.sun.istack.internal.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
 */
public class JSON {
	private static final JSONParser parser = new JSONParser();

	public static JsonObject parse(@NotNull String json) throws ParseException {
		return new JsonObject((JSONObject) parser.parse(json));
	}

	static JsonArray convertToJsonArray(Object value) {
		if (value == null) return null;
		else if (value instanceof JSONArray) return new JsonArray((JSONArray) value);
		else throw new ClassCastException("Invalid Cast");
	}

	static JsonObject convertToJsonObject(Object value) {
		if (value == null) return null;
		else if (value instanceof JSONObject) return new JsonObject((JSONObject) value);
		else throw new ClassCastException("Invalid Cast");
	}

	static String convertToJsonString(Object value) {
		if (value == null) return null;
		else if (value instanceof String) return (String) value;
		else throw new ClassCastException("Invalid Cast");
	}

	static Double convertToJsonDouble(Object value) {
		if (value == null) return null;
		else if (value instanceof Double) return (Double) value;
		else throw new ClassCastException("Invalid Cast");
	}

	static Long convertToJsonLong(Object value) {
		if (value == null) return null;
		else if (value instanceof Long) return (Long) value;
		else throw new ClassCastException("Invalid Cast");
	}

	static Boolean convertToJsonBool(Object value) {
		if (value == null) return null;
		else if (value instanceof Boolean) return (Boolean) value;
		else throw new ClassCastException("Invalid Cast");
	}
}
