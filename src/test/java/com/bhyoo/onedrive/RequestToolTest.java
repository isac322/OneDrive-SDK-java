package com.bhyoo.onedrive;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.container.items.DriveItem;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.DriveItemFuture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
class RequestToolTest {

	private static Client client;
	private static RequestTool requestTool;

	@BeforeAll
	static void getClient() {
		assertNull(client);

		final String clientId = "f21d2eff-49e2-4a10-a515-4a077f23c694";
		final String[] scope = {"files.readwrite.all", "offline_access"};
		final String redirectURL = "http://localhost:8080/";
		final String clientSecret = "1t5UhiBewLrVUoKqWZWYiiS";

		client = new Client(clientId, scope, redirectURL, clientSecret);

		assertNotNull(client);
		assertTrue(client.isLogin());
		assertFalse(client.isExpired());

		assertNotNull(client.getAccessToken());
		assertNotNull(client.getAuthCode());
		assertNotNull(client.getClientId());
		assertNotNull(client.getClientSecret());
		assertNotNull(client.getFullToken());
		assertNotNull(client.getRedirectURL());
		assertNotNull(client.getRefreshToken());
		assertNotNull(client.getTokenType());
		assertArrayEquals(client.getScopes(), scope);
		assertNotEquals(client.getExpirationTime(), 0L);

		requestTool = client.requestTool();

		System.out.println(client.getFullToken());
	}

	@AfterAll
	static void logout() throws Exception {
		assertNotNull(client);
		assertTrue(client.isLogin());

		//		client.logout();

		//		assertFalse(client.isLogin());
	}

	@Test void getItemAsync() throws Exception {
		DriveItemFuture future2 = requestTool.getItemAsync("/drive/items/D4FD82CA6DF96A47!25786")
				.syncUninterruptibly();
/*		DriveItemFuture future = requestTool.getItemAsync(Client.ITEM_ID_PREFIX + "D4FD82CA6DF96A47!25784")
				.syncUninterruptibly();

		if (future.isSuccess()) {
			System.out.println(future.get());
		}*/
	}

	@Test void getItem() throws ErrorResponseException {
		long before = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			long l = System.currentTimeMillis();
			DriveItem item = requestTool.getItem(Client.ITEM_ID_PREFIX + ClientTest.MP3_UTF8_BIG);
			System.out.println(System.currentTimeMillis() - l);
		}
		System.out.println(System.currentTimeMillis() - before);
	}

	@Test void getItem1() throws ErrorResponseException {
		DriveItem item = requestTool.getItem(Client.ITEM_ID_PREFIX + "D4FD82CA6DF96A47!25784");
		System.out.println(item.getName());
		System.out.println(item.getId());
	}
}