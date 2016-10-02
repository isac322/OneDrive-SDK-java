package org.simpler.json;

import com.sun.istack.internal.NotNull;
import org.json.simple.JSONObject;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
 */
public class JsonObject {
	private final JSONObject jsonObject;

	public JsonObject(@NotNull JSONObject object) {
		jsonObject = object;
	}

	public JsonObject getObject(@NotNull String key) {
		Object value = jsonObject.get(key);

		return JSON.convertToJsonObject(value);
	}

	public JsonArray getArray(@NotNull String key) {
		Object value = jsonObject.get(key);

		return JSON.convertToJsonArray(value);
	}

	public String getString(@NotNull String key) {
		Object value = jsonObject.get(key);

		return JSON.convertToJsonString(value);
	}

	public Double getDouble(@NotNull String key) {
		Object value = jsonObject.get(key);

		return JSON.convertToJsonDouble(value);
	}

	public Long getLong(@NotNull String key) {
		Object value = jsonObject.get(key);

		return JSON.convertToJsonLong(value);
	}

	public Boolean getBoolean(@NotNull String key) {
		Object value = jsonObject.get(key);

		return JSON.convertToJsonBool(value);
	}

	@Override
	public String toString() {
		return jsonObject.toString();
	}

	public boolean contains(String key) {
		return jsonObject.containsKey(key);
	}
}
