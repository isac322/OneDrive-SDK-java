package com.bhyoo.onedrive;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.Drive;
import com.bhyoo.onedrive.container.items.BaseItem;
import com.bhyoo.onedrive.container.items.FolderItem;
import com.bhyoo.onedrive.container.items.ResponsePage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ClientTest {
	public static String DIR_MANY_CHILD = "D4FD82CA6DF96A47!14841";
	public static String DIR_MANY_CHILD_NAME = "BOJ";
	public static String MP3_UTF8_BIG = "D4FD82CA6DF96A47!24998";
	public static String MP3_UTF8_BIG_NAME = "[수정] vol.5.mp3";
	public static String MP3_UTF8_SMALL = "D4FD82CA6DF96A47!25997";
	public static String MP3_UTF8_SMALL_NAME = "Roy Kim 로이킴 - 봄봄봄. -[ mymusicroad.net ].mp3";
	public static String TXT_ASCII_SMALL = "D4FD82CA6DF96A47!26026";
	public static String TXT_ASCII_WITH_SPACE = "D4FD82CA6DF96A47!26036";
	public static String TXT_ASCII_ESCAPED = "D4FD82CA6DF96A47%2126037";
	public static String DIR_SHARED_BY_SOMEONE = "485BEF1A80539148!115";
	public static String PACKAGE_1 = "D4FD82CA6DF96A47!22159";
	public static String PACKAGE_2 = "D4FD82CA6DF96A47!2104";
	private static Client client;

	@BeforeAll
	static void getClient() {
		assertNull(client);

		final String clientId = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";
		final String[] scope = {"files.readwrite.all", "offline_access"};
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
		assertArrayEquals(client.getScopes(), scope);
		assertNotEquals(client.getExpirationTime(), 0L);
	}

	@AfterAll
	static void logout() throws Exception {
		assertNotNull(client);
		assertTrue(client.isLogin());

		client.logout();

		assertFalse(client.isLogin());
	}

	private static void testDrive(Drive drive) {
		assertNotNull(drive.getDeleted());
		assertNotNull(drive.getDriveType());
		assertNotNull(drive.getId());
		assertNotNull(drive.getRemaining());
		assertNotNull(drive.getState());
		assertNotNull(drive.getTotalCapacity());
		assertNotNull(drive.getUsedCapacity());
	}

	@Test void refreshLogin() throws Exception {
		String accessToken = client.getAccessToken();
		String authCode = client.getAuthCode();
		String clientId = client.getClientId();
		String clientSecret = client.getClientSecret();
		String redirectURL = client.getRedirectURL();
		String refreshToken = client.getRefreshToken();
		String tokenType = client.getTokenType();
		String[] scopes = client.getScopes();
		long expirationTime = client.getExpirationTime();

		client.refreshLogin();

		assertTrue(client.isLogin());
		assertFalse(client.isExpired());

		assertNotEquals(client.getAccessToken(), accessToken);
		assertEquals(client.getAuthCode(), authCode);
		assertEquals(client.getClientId(), clientId);
		assertEquals(client.getClientSecret(), clientSecret);
		assertEquals(client.getRedirectURL(), redirectURL);
		assertNotEquals(client.getRefreshToken(), refreshToken);
		assertEquals(client.getTokenType(), tokenType);
		assertArrayEquals(client.getScopes(), scopes);
		assertNotEquals(client.getExpirationTime(), expirationTime);
	}

	@Test void getDefaultDrive() throws Exception {
		Drive defaultDrive = client.getDefaultDrive();

		testDrive(defaultDrive);
	}

	@Test void getAllDrive() throws Exception {
		Drive[] drives = client.getAllDrive();

		for (Drive drive : drives) testDrive(drive);
	}

	@Test void getRootDir() throws Exception {
		FolderItem rootDir = client.getRootDir();

		assertTrue(rootDir.isRoot());
		assertNotNull(rootDir.getParentReference());
	}

	// FIXME
	@Test void getFolder() throws Exception {
		FolderItem folder = client.getFolder(DIR_MANY_CHILD);

		assertEquals(folder.getName(), DIR_MANY_CHILD_NAME);
		assertEquals(folder.getId(), DIR_MANY_CHILD);
		assertEquals(folder.childrenCount(), folder.allChildren().size());
		assertFalse(folder.isRoot());
		assertTrue(folder.isChildrenFetched());

		for (BaseItem item : folder) {
			System.out.println(item.getName() + '\t' + item.getSize());
		}
	}

	@Test void getFolder1() throws Exception {
		FolderItem folder = client.getFolder(DIR_MANY_CHILD, false);

		assertEquals(folder.getName(), DIR_MANY_CHILD_NAME);
		assertEquals(folder.getId(), DIR_MANY_CHILD);
		assertFalse(folder.isRoot());
		assertFalse(folder.isChildrenFetched());
		folder.allChildren();
	}

	// FIXME
	@Test void getItem() throws Exception {
		long before = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			long l = System.currentTimeMillis();
			BaseItem item = client.getItem(MP3_UTF8_BIG);
			System.out.println(System.currentTimeMillis() - l);
		}
		System.out.println(System.currentTimeMillis() - before);
	}

	// FIXME
	@Test void searchItem() throws Exception {
		@NotNull ResponsePage<BaseItem> items = client.searchItem("1학기");

		for (BaseItem item : items.getValue()) {
			System.out.println(item.getClass().getName() + '\t' + item.getName());
		}
	}

	@Test void getFullToken() throws Exception {
		assertEquals(client.getFullToken(), client.getTokenType() + ' ' + client.getAccessToken());
	}

}
