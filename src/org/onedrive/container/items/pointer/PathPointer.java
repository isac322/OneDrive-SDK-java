package org.onedrive.container.items.pointer;

import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@// TODO: Enhance javadoc}
 * {@// TODO: this class can be static factory!. consider this later (if path is same, give caller same object to
 * save memory}
 * In OneDrive, <a href='https://dev.onedrive.com/README.htm#item-resource'>item</a> referencing can be represented by
 * both <b><tt>ID</tt></b> and <b><tt>path</tt></b>.<br>
 * Unlike <b><tt>ID</tt></b>, usage of <b><tt>path</tt></b> is not quite simple.<br>
 * All <b><tt>path</tt></b> referencing must fallow <tt>/drive/root:/{item-path}</tt> or
 * <tt>/drives/{drive-id}/root:/{item-path}</tt> form.<br>
 * <br>
 * This class helps programmer to reference file with path notation.
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class PathPointer extends BasePointer {
	private final static Pattern pathMatcher = Pattern.compile("^/drive/root:(.*)$");
	private final static Pattern drivePathMatcher = Pattern.compile("^/drives/([a-fA-F0-9]+)/root:(.*)$");
	@Getter @Nullable private final String driveId;
	@Getter @NotNull private final String readablePath;
	@NotNull private final String path;
	@NotNull private final String rawPath;


	@SneakyThrows({UnsupportedEncodingException.class, URISyntaxException.class})
	public PathPointer(@NotNull String anyPath) {
		String rawPath = anyPath;

		// ensure that `anyPath` is always decoded path
		anyPath = URLDecoder.decode(rawPath, "UTF-8");

		// if `anyPath` is already escaped string, make encoded path
		if (anyPath.equalsIgnoreCase(rawPath)) rawPath = new URI(null, null, rawPath, null).toASCIIString();


		Matcher matcher;

		if ((matcher = drivePathMatcher.matcher(anyPath)).matches()) {
			this.readablePath = matcher.group(2);
			this.path = anyPath;
			this.driveId = matcher.group(1);
			this.rawPath = rawPath;
		}
		else if ((matcher = pathMatcher.matcher(anyPath)).matches()) {
			this.readablePath = matcher.group(1);
			this.path = anyPath;
			this.driveId = null;
			this.rawPath = rawPath;
		}
		else if (anyPath.charAt(0) != '/') {
			throw new IllegalArgumentException("path argument must starts with '/'. current path string : " + anyPath);
		}
		else {
			this.readablePath = anyPath;
			this.path = "/drive/root:" + anyPath;
			this.driveId = null;
			this.rawPath = "/drive/root:" + rawPath;
		}
	}


	@SneakyThrows({UnsupportedEncodingException.class, URISyntaxException.class})
	public PathPointer(@NotNull String anyPath, @Nullable String driveId) {
		String rawPath = anyPath;

		// ensure that `anyPath` is always decoded path
		anyPath = URLDecoder.decode(rawPath, "UTF-8");

		// if `anyPath` is already escaped string, make encoded path
		if (anyPath.equalsIgnoreCase(rawPath)) rawPath = new URI(null, null, rawPath, null).toASCIIString();


		Matcher matcher;

		// anyPath is OneDrive-path notation that contains drive-id
		if ((matcher = drivePathMatcher.matcher(anyPath)).matches()) {
			this.readablePath = matcher.group(2);
			this.driveId = driveId;

			// index of path-value separator
			int separatorIdx = anyPath.indexOf(':');

			// if `driveId` is null, `anyPath`'s drive-id will be removed
			if (driveId == null) {
				this.path = "/drive/root:" + this.readablePath;
				this.rawPath = "/drive/root" + rawPath.substring(separatorIdx);
			}
			// set drive-id value in `anyPath` to `driveId`
			else {
				this.path = "/drives/" + driveId + "/root:" + this.readablePath;
				this.rawPath = "/drives/" + driveId + "/root" + rawPath.substring(separatorIdx);
			}
		}
		// anyPath is OneDrive-path notation that does not contain drive-id
		else if ((matcher = pathMatcher.matcher(anyPath)).matches()) {
			this.readablePath = matcher.group(1);
			this.driveId = driveId;

			if (driveId == null) {
				this.path = anyPath;
				this.rawPath = rawPath;
			}
			else {
				this.path = "/drives/" + driveId + "/root:" + this.readablePath;
				// `rawPath` start with '/drive/root:' and `/root:`'s start index is 6.
				this.rawPath = "/drives/" + driveId + rawPath.substring(6);
			}
		}
		// if anyPath is neither OneDrive-path nor pure absolute path
		else if (anyPath.charAt(0) != '/') {
			throw new IllegalArgumentException("path argument must starts with '/'. current path is : " + anyPath);
		}
		// if anyPath is pure absolute path
		else {
			this.readablePath = anyPath;
			this.driveId = driveId;

			if (driveId == null) {
				this.path = "/drive/root:" + anyPath;
				this.rawPath = "/drive/root:" + rawPath;
			}
			else {
				this.path = "/drives/" + driveId + "/root:" + anyPath;
				this.rawPath = "/drives/" + driveId + "/root:" + rawPath;
			}
		}
	}

	private PathPointer(@Nullable String driveId, @NotNull String readablePath,
						@NotNull String path, @NotNull String rawPath) {
		this.driveId = driveId;
		this.readablePath = readablePath;
		this.path = path;
		this.rawPath = rawPath;
	}

	@NotNull
	@Override
	public URI toURI() throws URISyntaxException {
		return new URI("https", null, path, null);
	}

	@NotNull
	@Override
	public String toApi() {
		return path;
	}

	@NotNull
	@Override
	public String resolveOperator(@NotNull String operator) {
		return rawPath + ":/" + operator;
	}

	@NotNull
	@Override
	public String toASCIIApi() {
		return rawPath;
	}

	@NotNull
	@Override
	public String toJson() {
		return "{\"path\":\"" + path + "\"}";
	}


	@NotNull
	@SneakyThrows({UnsupportedEncodingException.class, URISyntaxException.class})
	public PathPointer resolve(@NotNull String name) {
		// raise exception if `name` is absolute path
		if (name.charAt(0) == '/') {
			throw new IllegalArgumentException("name argument must not starts with '/'. current name is : " + name);
		}

		String rawName = name;

		// ensure that `name` is always decoded string
		name = URLDecoder.decode(name, "UTF-8");

		// if `anyPath` is already escaped string, make encoded path
		if (name.equalsIgnoreCase(rawName)) rawName = new URI(null, null, name, null).toASCIIString();

		if (path.charAt(path.length() - 1) == '/')
			return new PathPointer(driveId, readablePath + name, path + name, rawPath + rawName);
		else
			return new PathPointer(driveId, readablePath + '/' + name, path + '/' + name, rawPath + '/' + rawName);
	}
}
