import junit.framework.TestCase;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.Drive;
import org.onedrive.container.items.BaseItem;
import org.onedrive.container.items.FileItem;
import org.onedrive.container.items.FolderItem;
import org.onedrive.container.items.PackageItem;
import org.onedrive.utils.OneDriveRequest;

import java.util.concurrent.TimeUnit;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
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

		HttpsResponse response = OneDriveRequest.doGet("/drive/root?expand=children", client.getAccessToken());
		System.out.println(response.getContentString());
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
	}

	private void dfs(FolderItem folder, String tab) {
		System.out.print(tab);
		System.out.println(folder.getName());

		tab += '\t';
		for (BaseItem item : folder) {
			if (item instanceof FolderItem) {
				dfs((FolderItem) item, tab);
			}
			else if (item instanceof FileItem) {
				System.out.print(tab);
				System.out.println(item.getName());
			}
			else if (item instanceof PackageItem) {
				System.out.println(item.getName());
			}
			else {
				throw new RuntimeException("Unsupported type item.");
			}
		}
	}

	public void testRecursiveTravel() {
		getClient();

		dfs(client.getRootDir(), "");
	}
}
