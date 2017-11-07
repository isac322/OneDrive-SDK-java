package com.bhyoo.onedrive.network.sync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.QueryStringEncoder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SyncRequestTest {
	static final TypeReference<HashMap<String, String>[]> ARRAY_TYPE_REF
			= new TypeReference<HashMap<String, String>[]>() {};
	static final TypeReference<HashMap<String, String>> TYPE_REFERENCE
			= new TypeReference<HashMap<String, String>>() {};
	static final String TEST_SERVER = "https://jsonplaceholder.typicode.com";
	static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	static HashMap<String, String>[] testPosts;

	static String convertMapToForm(Map<String, String> map) {
		QueryStringEncoder encoder = new QueryStringEncoder("");

		for (Map.Entry<String, String> entry : map.entrySet()) {
			encoder.addParam(entry.getKey(), entry.getValue());
		}
		return encoder.toString().substring(1);
	}

	@BeforeAll
	static void doGet() throws IOException {
		SyncRequest request = new SyncRequest(TEST_SERVER + "/posts");
		SyncResponse response = request.doGet();

		response.getContentString();
		assertEquals(response.contentString, response.getContentString());
		assertEquals(200, response.code);

		testPosts = OBJECT_MAPPER.readValue(response.getContent(), ARRAY_TYPE_REF);
	}

	@Test
	void doPost() throws IOException {
		Random random = new Random(System.currentTimeMillis());
		int postIndex = random.nextInt(testPosts.length);

		HashMap<String, String> aPost = testPosts[postIndex];
		aPost.remove("id");

		SyncResponse response = new SyncRequest(TEST_SERVER + "/posts")
				.setHeader(ACCEPT, APPLICATION_JSON)
				.setHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
				.doPost(convertMapToForm(aPost));
		assertEquals(201, response.getCode());


		HashMap<String, String> result = OBJECT_MAPPER.readValue(response.getContent(), TYPE_REFERENCE);

		assertEquals(String.valueOf(testPosts.length + 1), result.get("id"));
		assertEquals(aPost.get("title"), result.get("title"));
		assertEquals(aPost.get("body"), result.get("body"));
		assertEquals(aPost.get("userId"), result.get("userId"));
	}

	@Test
	void doPut() throws IOException {
		Random random = new Random(System.currentTimeMillis());
		int postIndex = random.nextInt(testPosts.length);

		HashMap<String, String> aPost = testPosts[postIndex];
		HashMap<String, String> newPost = ((HashMap<String, String>) aPost.clone());
		newPost.put("title", "foo");

		SyncResponse response = new SyncRequest(TEST_SERVER + "/posts/" + (postIndex + 1))
				.setHeader(ACCEPT, APPLICATION_JSON)
				.setHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
				.doPut(convertMapToForm(newPost));
		assertEquals(200, response.getCode());

		HashMap<String, String> result = OBJECT_MAPPER.readValue(response.getContent(), TYPE_REFERENCE);
		assertEquals("foo", result.get("title"));
		assertEquals(aPost.get("id"), result.get("id"));
		assertEquals(aPost.get("body"), result.get("body"));
		assertEquals(aPost.get("userId"), result.get("userId"));
	}

	@Test
	void doPatch() throws IOException {
		Random random = new Random(System.currentTimeMillis());
		int postIndex = random.nextInt(testPosts.length);

		HashMap<String, String> aPost = testPosts[postIndex];
		HashMap<String, String> newPost = new HashMap<>();
		newPost.put("title", "foo");

		SyncResponse response = new SyncRequest(TEST_SERVER + "/posts/" + (postIndex + 1))
				.setHeader(ACCEPT, APPLICATION_JSON)
				.setHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
				.doPatch(convertMapToForm(newPost));
		assertEquals(200, response.getCode());

		HashMap<String, String> result = OBJECT_MAPPER.readValue(response.getContent(), TYPE_REFERENCE);
		assertEquals("foo", result.get("title"));
		assertEquals(aPost.get("id"), result.get("id"));
		assertEquals(aPost.get("body"), result.get("body"));
		assertEquals(aPost.get("userId"), result.get("userId"));
	}

	@Test
	void doDelete() {
		Random random = new Random(System.currentTimeMillis());
		int postIndex = random.nextInt(testPosts.length);

		SyncResponse response = new SyncRequest(TEST_SERVER + "/posts/" + (postIndex + 1))
				.doDelete();
		assertEquals(200, response.getCode());
	}

	@Test
	void doubleSend() {
		Random random = new Random(System.currentTimeMillis());
		int postIndex = random.nextInt(testPosts.length);

		final SyncRequest request = new SyncRequest(TEST_SERVER + "/posts/" + (postIndex + 1));
		SyncResponse response = request.doDelete();
		assertEquals(200, response.getCode());

		assertThrows(IllegalStateException.class, new Executable() {
			@Override public void execute() throws Throwable {
				request.doDelete();
			}
		});
	}
}