package org.simpler.json;

import com.sun.istack.internal.NotNull;
import org.json.simple.JSONArray;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
 */
public class JsonArray {
	private final JSONArray jsonArray;

	public JsonArray(@NotNull JSONArray array) {
		jsonArray = array;
	}

	public JsonObject getObject(int index) {
		Object value = jsonArray.get(index);

		return JSON.convertToJsonObject(value);
	}

	public JsonArray getArray(int index) {
		Object value = jsonArray.get(index);

		return JSON.convertToJsonArray(value);
	}

	public String getString(int index) {
		Object value = jsonArray.get(index);

		return JSON.convertToJsonString(value);
	}

	public Double getDouble(int index) {
		Object value = jsonArray.get(index);

		return JSON.convertToJsonDouble(value);
	}

	public Long getLong(int index) {
		Object value = jsonArray.get(index);

		return JSON.convertToJsonLong(value);
	}

	public Boolean getBoolean(int index) {
		Object value = jsonArray.get(index);

		return JSON.convertToJsonBool(value);
	}

	@Override
	public String toString() {
		return jsonArray.toString();
	}
}
