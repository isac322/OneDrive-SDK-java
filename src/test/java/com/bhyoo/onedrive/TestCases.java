package com.bhyoo.onedrive;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.AsyncJobMonitor;
import com.bhyoo.onedrive.container.AsyncJobStatus;
import com.bhyoo.onedrive.container.items.DriveItem;
import com.bhyoo.onedrive.container.items.FileItem;
import com.bhyoo.onedrive.container.items.FolderItem;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class TestCases {
	private static final String TEST_DIR_NAME = "___for_test";
	private static Client client;

	@BeforeAll
	static void getClient() {
		assertNull(client);

		final String clientId = "0c4b69a8-adac-4ec1-b310-6c28ff9fa263";
		final String[] scope = {"files.readwrite.all", "offline_access"};
		final String redirectURL = "http://localhost:8080/";
		final String clientSecret = "iqggDGYW25$;#$otqVUH024";

		client = new Client(clientId, scope, redirectURL, clientSecret);


		assertNotNull(client);
		assertTrue(client.isLogin());
		assertFalse(client.isExpired());

		System.out.println(client.getFullToken());

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
	static void logout() throws ErrorResponseException {
		assertNotNull(client);
		assertTrue(client.isLogin());

		client.deleteItem(PathPointer.root.resolve(TEST_DIR_NAME));

		client.logout();

		assertFalse(client.isLogin());
	}

	/**
	 * Scenario:
	 * <p>
	 * 1. create a directory on root dir
	 * 2. upload an audio file to the dir
	 * 3. rename the file
	 * 4. copy the file to same directory with another name
	 */
	@Test
	void createDirAndUpload() throws ErrorResponseException, URISyntaxException {
		// Create directory '___for_test' in root directory of default drive of current account
		FolderItem testDir = client.createFolder(PathPointer.root, TEST_DIR_NAME);

		assertEquals(TEST_DIR_NAME, testDir.getName());
		assertEquals(
				new PathPointer("/" + TEST_DIR_NAME, testDir.getDriveId()).toASCIIApi(),
				testDir.getPathPointer().toASCIIApi());
		assertEquals(0L, testDir.getSize().longValue());
		assertFalse(testDir.isDeleted());
		assertFalse(testDir.isRoot());
		assertFalse(testDir.isSpecial());
		assertArrayEquals(new DriveItem[0], testDir.allChildren());

		URL resource = ClassLoader.getSystemClassLoader().getResource("SampleAudio_0.7mb.mp3");
		assertNotNull(resource);

		Path path = Paths.get(resource.toURI());
		// Upload local file to '___for_test' asynchronously
		FileItem uploaded = (FileItem) testDir.simpleUploadFileAsync(path).awaitUninterruptibly().getNow();

		assertNotNull(uploaded);
		assertEquals("SampleAudio_0.7mb.mp3", uploaded.getName());
		assertEquals(TEST_DIR_NAME, uploaded.getParentReference().getPathPointer().getName());

		// Rename the uploaded file to 'modified.mp3'
		uploaded.rename("modified.mp3");

		assertNotNull(uploaded);
		assertEquals("modified.mp3", uploaded.getName());

		// Copy the uploaded file to same directory with another name 'copied.mp3'
		@NotNull AsyncJobMonitor monitor = uploaded.copyTo(testDir, "copied.mp3");

		System.out.println(monitor.getPercentageComplete());
		// Wait until copy job is done
		while (monitor.getStatus() != AsyncJobStatus.COMPLETED) {
			monitor.update();
			System.out.println(monitor.getPercentageComplete());
		}

		assertTrue(testDir.isChildrenFetched());
		assertEquals(0, testDir.childCount());

		// Update children info `testDir`
		testDir.fetchChildren();

		assertTrue(testDir.isChildrenFetched());
		assertEquals(2, testDir.allChildren().length);
		for (DriveItem item : testDir.allChildren()) {
			assertTrue(item.getName().equals("copied.mp3") || item.getName().equals("modified.mp3"));
		}
	}
}
