package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/file_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class FileFacet {
	@Getter @Nullable protected final String mimeType;
	@Getter @Nullable protected final String sha1Hash;
	@Getter @Nullable protected final String crc32Hash;
	@Getter @Nullable protected final String quickXorHash;

	protected FileFacet(String mimeType, String sha1Hash, String crc32Hash, String quickXorHash) {
		this.mimeType = mimeType;
		this.sha1Hash = sha1Hash;
		this.crc32Hash = crc32Hash;
		this.quickXorHash = quickXorHash;
	}

	@Nullable
	public static FileFacet parse(JSONObject json) {
		if (json == null) return null;

		if (json.containsKey("hashes")) {
			JSONObject hashes = json.getObject("hashes");

			return new FileFacet(
					json.getString("mimeType"),
					hashes.getString("crc32Hash"),
					hashes.getString("sha1Hash"),
					hashes.getString("quickXorHash")
			);
		}
		else {
			return new FileFacet(
					json.getString("mimeType"),
					null, null, null
			);
		}
	}
}
