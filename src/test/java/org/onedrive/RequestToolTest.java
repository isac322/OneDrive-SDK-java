package org.onedrive;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onedrive.container.items.BaseItem;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.network.async.BaseItemFuture;

import static org.junit.Assert.*;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class RequestToolTest {

	private static Client client;
	private static RequestTool requestTool;

	@BeforeClass
	public static void getClient() {
		assertNull(client);

		final String clientId = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";
		final String[] scope = {"onedrive.readwrite", "offline_access", "onedrive.appfolder"};
		final String redirectURL = "http://localhost:8080/";
		final String clientSecret = "XXXXXXXXXXXXXXXXXXXXXXX";

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
		assertNotNull(client.getUserId());
		assertArrayEquals(client.getScopes(), scope);
		assertNotEquals(client.getExpirationTime(), 0L);

		requestTool = client.requestTool();
	}

	@AfterClass
	public static void logout() throws Exception {
		assertNotNull(client);
		assertTrue(client.isLogin());

		client.logout();

		assertFalse(client.isLogin());
	}

	@Test
	public void getItemAsync() throws Exception {
		BaseItemFuture future = requestTool.getItemAsync(Client.ITEM_ID_PREFIX + ClientTest.MP3_UTF8_BIG)
				.syncUninterruptibly();

		System.out.println(future.get());
	}

	@Test
	public void getItem() throws ErrorResponseException {
		long before = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			long l = System.currentTimeMillis();
			BaseItem item = requestTool.getItem(Client.ITEM_ID_PREFIX + ClientTest.MP3_UTF8_BIG);
			System.out.println(System.currentTimeMillis() - l);
		}
		System.out.println(System.currentTimeMillis() - before);
	}
}