package org.onedrive;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onedrive.container.Drive;
import org.onedrive.container.items.BaseItem;
import org.onedrive.container.items.FolderItem;
import org.onedrive.container.items.ResponsePage;

import static org.junit.Assert.*;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ClientTest {
	private static String DIR_MANY_CHILD = "D4FD82CA6DF96A47!14841";
	private static String DIR_MANY_CHILD_NAME = "BOJ";
	private static String MP3_UTF8_BIG = "D4FD82CA6DF96A47!24998";
	private static String MP3_UTF8_BIG_NAME = "[수정] vol.5.mp3";
	private static String MP3_UTF8_SMALL = "D4FD82CA6DF96A47!25997";
	private static String MP3_UTF8_SMALL_NAME = "Roy Kim 로이킴 - 봄봄봄. -[ mymusicroad.net ].mp3";
	private static String TXT_ASCII_SMALL = "D4FD82CA6DF96A47!26026";
	private static String TXT_ASCII_WITH_SPACE = "D4FD82CA6DF96A47!26036";
	private static String TXT_ASCII_ESCAPED = "D4FD82CA6DF96A47%2126037";
	private static String DIR_SHARED_BY_SOMEONE = "485BEF1A80539148!115";
	private static String PACKAGE_1 = "D4FD82CA6DF96A47!22159";
	private static String PACKAGE_2 = "D4FD82CA6DF96A47!2104";

	private static Client client;

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
	}

	@AfterClass
	public static void logout() throws Exception {
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

	@Test
	public void refreshLogin() throws Exception {
		String accessToken = client.getAccessToken();
		String authCode = client.getAuthCode();
		String clientId = client.getClientId();
		String clientSecret = client.getClientSecret();
		String redirectURL = client.getRedirectURL();
		String refreshToken = client.getRefreshToken();
		String tokenType = client.getTokenType();
		String userId = client.getUserId();
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
		assertEquals(client.getUserId(), userId);
		assertArrayEquals(client.getScopes(), scopes);
		assertNotEquals(client.getExpirationTime(), expirationTime);
	}

	@Test
	public void getDefaultDrive() throws Exception {
		Drive defaultDrive = client.getDefaultDrive();

		testDrive(defaultDrive);
	}

	@Test
	public void getAllDrive() throws Exception {
		Drive[] drives = client.getAllDrive();

		for (Drive drive : drives) testDrive(drive);
	}

	@Test
	public void getRootDir() throws Exception {
		FolderItem rootDir = client.getRootDir();

		assertTrue(rootDir.isRoot());
		assertNull(rootDir.getParentReference());
	}

	@Test
	public void getFolder() throws Exception {
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

	@Test
	public void getFolder1() throws Exception {
		FolderItem folder = client.getFolder(DIR_MANY_CHILD, false);

		assertEquals(folder.getName(), DIR_MANY_CHILD_NAME);
		assertEquals(folder.getId(), DIR_MANY_CHILD);
		assertFalse(folder.isRoot());
		assertFalse(folder.isChildrenFetched());
		folder.allChildren();
	}

	@Test
	public void getFolder2() throws Exception {

	}

	@Test
	public void getFolder3() throws Exception {

	}

	@Test
	public void getFile() throws Exception {

	}

	@Test
	public void getFile1() throws Exception {

	}

	@Test
	public void getItem() throws Exception {

	}

	@Test
	public void getItem1() throws Exception {

	}

	@Test
	public void getShared() throws Exception {

	}

	@Test
	public void copyItem() throws Exception {

	}

	@Test
	public void copyItem1() throws Exception {

	}

	@Test
	public void copyItem2() throws Exception {

	}

	@Test
	public void copyItem3() throws Exception {

	}

	@Test
	public void copyItem4() throws Exception {

	}

	@Test
	public void copyItem5() throws Exception {

	}

	@Test
	public void copyItem6() throws Exception {

	}

	@Test
	public void copyItem7() throws Exception {

	}

	@Test
	public void moveItem() throws Exception {

	}

	@Test
	public void moveItem1() throws Exception {

	}

	@Test
	public void moveItem2() throws Exception {

	}

	@Test
	public void moveItem3() throws Exception {

	}

	@Test
	public void createFolder() throws Exception {

	}

	@Test
	public void createFolder1() throws Exception {

	}

	@Test
	public void download() throws Exception {

	}

	@Test
	public void download1() throws Exception {

	}

	@Test
	public void download2() throws Exception {

	}

	@Test
	public void download3() throws Exception {

	}

	@Test
	public void downloadAsync() throws Exception {

	}

	@Test
	public void downloadAsync1() throws Exception {

	}

	@Test
	public void downloadAsync2() throws Exception {

	}

	@Test
	public void downloadAsync3() throws Exception {

	}

	@Test
	public void uploadFile() throws Exception {

	}

	@Test
	public void uploadFile1() throws Exception {

	}

	@Test
	public void uploadFile2() throws Exception {

	}

	@Test
	public void deleteItem() throws Exception {

	}

	@Test
	public void deleteItem1() throws Exception {

	}

	@Test
	public void searchItem() throws Exception {
		@NotNull ResponsePage<BaseItem> items = client.searchItem("1학기");

		for (BaseItem item : items.getValue()) {
			System.out.println(item.getClass().getName() + '\t' + item.getName());
		}
	}

	@Test
	public void getFullToken() throws Exception {
		assertEquals(client.getFullToken(), client.getTokenType() + ' ' + client.getAccessToken());
	}

}
