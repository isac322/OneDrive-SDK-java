import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.Drive;
import org.onedrive.container.facet.AudioFacet;
import org.onedrive.container.items.*;
import org.onedrive.utils.OneDriveRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * TODO: add javadoc
 */
public class ClientTest extends TestCase {
	private static Client client;

	private static Client getClient() {

			client = new Client(clientId, scope, redirectURL, clientSecret);
		}

		return client;
	}

	public void testLogin() {
		getClient();

		assertNotNull(client.getAccessToken());
		assertNotNull(client.getAuthCode());
		assertNotNull(client.getRefreshToken());

		assertTrue(client.isLogin());
	}

	public void testExpiration() throws InterruptedException {
		getClient();

		assertTrue(client.isLogin());

		TimeUnit.SECONDS.sleep(client.getExpirationTime());

		assertFalse(client.isExpired());
	}

	public void testLogout() {
		getClient();

		client.logout();

		assertFalse(client.isLogin());
	}

	public void testItem() {
		getClient();

		HttpsResponse response = OneDriveRequest.doGet("/drive/shared?select=id", client.getAccessToken());
		System.out.println(response.getContentString());
	}

	public void testGetFileItem() {
		getClient();

		FileItem file = client.getFile("D4FD82CA6DF96A47!22159");
		System.out.println(file.getName());
		System.out.println(file.getId());
	}

	public void testSharedItem() {
		getClient();

		BaseItem[] shared = client.getShared();

		for (BaseItem item : shared) {
			System.out.println(item.getId());
			System.out.println(item.getName());
		}
	}

	public void testDrives() {
		getClient();

		for (Drive drive : client.getAllDrive()) {
			System.out.println(drive.getDriveType());
			System.out.println(drive.getId());
			System.out.println(drive.getTotalCapacity());
		}
	}

	public void testDrive() {
		getClient();

		Drive defaultDrive = client.getDefaultDrive();

		System.out.println(defaultDrive);
	}

	public void testPackage() {
		getClient();

		HttpsResponse response = OneDriveRequest.doGet("/drive/items/485BEF1A80539148!115?expand=children", client
				.getAccessToken());
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testRemoteItem() {
		getClient();

		HttpsResponse response = OneDriveRequest.doGet("/drives/485bef1a80539148/items/485BEF1A80539148!115?expand=children", client
				.getAccessToken());
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testGetItem() {
		getClient();

		// BOJ
		FolderItem folder = client.getFolder("D4FD82CA6DF96A47!14841");
		System.out.println(folder.getName());
		for (BaseItem item : folder) {
			System.out.println(item.getName());
		}
	}

	public void testRoot() {
		getClient();

		HttpsResponse response = OneDriveRequest.doGet("/drive/root:/?expand=children", client.getAccessToken());
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testRootDir() {
		getClient();

		FolderItem rootDir = client.getRootDir();
		System.out.println(rootDir.getId());
		System.out.println(rootDir.getCTag());
		System.out.println(rootDir.getETag());
		System.out.println(rootDir.getName());
		System.out.println(rootDir.getSize());
		System.out.println(rootDir.getFolderChildren().get(1).getAllChildren().get(0)
				.getParentReference()
				.getPath());
		System.out.println(rootDir.getFolderChildren().get(1));
	}

	private void dfs(FolderItem folders, String tab) throws IOException, FileDownFailException {
		StringBuilder builder2 = new StringBuilder(tab).append(folders.getName()).append('\t');

		if (folders instanceof RemoteFolderItem) builder2.append(" Remote Item");
		if (folders.getSearchResult() != null) builder2.append(" Search result");
		if (folders.getShared() != null) builder2.append(" Shared");

		System.out.println(builder2.toString());

		tab += '\t';
		for (BaseItem item : folders) {
			if (item instanceof FolderItem) {
				FolderItem folder = (FolderItem) item;

				assertNotNull(folder.getFolder());
				assertNotNull(folder.getParentReference());
				assertEquals(folder.childrenCount(), folder.getAllChildren().size());
				dfs(folder, tab);
			}
			else if (item instanceof FileItem) {
				StringBuilder builder = new StringBuilder(tab).append(item.getName()).append('\t');
				assertNotNull(item.getParentReference());

				FileItem file = (FileItem) item;
				if (file.getAudio() != null) builder.append(" Audio");
				if (file.getImage() != null) builder.append(" Image");
				if (file.getLocation() != null) builder.append(" has Location");
				if (file.getPhoto() != null) builder.append(" Photo");
				if (file.getVideo() != null) builder.append(" Video");
				if (file.getSearchResult() != null) builder.append(" Search result");
				if (file.getShared() != null) builder.append(" Shared");

				System.out.println(builder.toString());

				int idx = file.getParentReference().getPath().indexOf(':') + 2;
				String path = file.getParentReference().getPath().substring(idx);
				file.download(Paths.get(path, file.getName()));
			}
			else if (item instanceof PackageItem) {
				System.out.println(tab + item.getName() + "\tPackage");
			}
			else {
				throw new RuntimeException("Unsupported type item.");
			}
		}
	}

	private void printDFS(FolderItem folders, String tab) throws IOException, FileDownFailException {
		StringBuilder builder2 = new StringBuilder(tab).append(folders.getName()).append('\t');

		if (folders instanceof RemoteFolderItem) builder2.append(" Remote Item");
		if (folders.getSearchResult() != null) builder2.append(" Search result");
		if (folders.getShared() != null) builder2.append(" Shared");

		System.out.println(builder2.toString());

		tab += '\t';
		for (BaseItem item : folders) {
			if (item instanceof FolderItem) {
				FolderItem folder = (FolderItem) item;

				printDFS(folder, tab);
			}
			else if (item instanceof FileItem) {
				StringBuilder builder = new StringBuilder(tab).append(item.getName()).append('\t');

				FileItem file = (FileItem) item;
				if (file.getAudio() != null) builder.append(" Audio");
				if (file.getImage() != null) builder.append(" Image");
				if (file.getLocation() != null) builder.append(" has Location");
				if (file.getPhoto() != null) builder.append(" Photo");
				if (file.getVideo() != null) builder.append(" Video");
				if (file.getSearchResult() != null) builder.append(" Search result");
				if (file.getShared() != null) builder.append(" Shared");

				System.out.println(builder.toString());
			}
			else if (item instanceof PackageItem) {
				System.out.println(tab + item.getName() + "\tPackage");
			}
			else {
				throw new RuntimeException("Unsupported type item.");
			}
		}
	}

	public void testRecursiveTravel() throws IOException, FileDownFailException {
		getClient();

		printDFS(client.getRootDir(), "");
	}

	public void testSearch() {
		getClient();

		HttpsResponse response = OneDriveRequest.doGet("/drive/root/view.search?q=Gone%20in%20Six%20Characters", client.getAccessToken());
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testJson() throws IOException {
		String json = "{\n" +
				"  \"album\": \"string\",\n" +
				"  \"albumArtist\": \"string\",\n" +
				"  \"artist\": \"string\",\n" +
				"  \"bitrate\": 128,\n" +
				"  \"composers\": \"string\",\n" +
				"  \"copyright\": \"string\",\n" +
				"  \"disc\": 0,\n" +
				"  \"discCount\": 0,\n" +
				"  \"duration\": 567,\n" +
				"  \"genre\": \"string\",\n" +
				"  \"hasDrm\": false,\n" +
				"  \"isVariableBitrate\": false,\n" +
				"  \"title\": \"string\",\n" +
				"  \"track\": 1,\n" +
				"  \"trackCount\": 16,\n" +
				"  \"year\": 2014\n" +
				"}";

		ObjectMapper mapper = new ObjectMapper();
		AudioFacet audio = mapper.readValue(json, AudioFacet.class);
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(audio));
	}
}
