package com.bhyoo.onedrive.container.items.pointer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathPointerTest {
	@Test
	void regularPath1Depth() {
		PathPointer pointer = new PathPointer("/test");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());
	}

	@Test
	void startsWithoutSlash() {
		assertThrows(IllegalArgumentException.class, () -> new PathPointer("name"));
	}

	@Test
	void withLeftSpaces() {
		PathPointer pointer = new PathPointer("   /test");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());

		pointer = new PathPointer("\t\t/test");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());
	}

	@Test
	void withRightSpaces() {
		PathPointer pointer = new PathPointer("/test   ");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());

		pointer = new PathPointer("/test\t\t");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());
	}

	@Test
	void withBothSpaces() {
		PathPointer pointer = new PathPointer("   /test   ");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());

		pointer = new PathPointer("\t\t/test\t\t");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/test", pointer.getReadablePath());
		assertEquals("/me/drive/root:/test", pointer.toApi());
		assertEquals("/me/drive/root:/test", pointer.toASCIIApi());
	}

	@Test
	void nonAscii() {
		PathPointer pointer = new PathPointer("/한글");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/한글", pointer.getReadablePath());
		assertEquals("/me/drive/root:/한글", pointer.toApi());
		assertEquals("/me/drive/root:/%ED%95%9C%EA%B8%80", pointer.toASCIIApi());
	}

	@Test
	void root() {
		PathPointer pointer = new PathPointer("/");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/", pointer.getReadablePath());
		assertEquals("/me/drive/root", pointer.toApi());
		assertEquals("/me/drive/root", pointer.toASCIIApi());
	}

	@Test
	void urlEncodedName() {
		PathPointer pointer = new PathPointer("/%fe%f2.txt");

		assertNull(pointer.getDriveId());
		assertNotNull(pointer.getReadablePath());

		assertEquals("/%fe%f2.txt", pointer.getReadablePath());
		assertEquals("/me/drive/root:/%fe%f2.txt", pointer.toApi());
		assertEquals("/me/drive/root:/%25fe%25f2.txt", pointer.toASCIIApi());
	}

	@Test
	void resolveTest() {
		PathPointer pointer = new PathPointer("/test");

		PathPointer resolved = pointer.resolve("inner");

		assertNull(resolved.getDriveId());
		assertNotNull(resolved.getReadablePath());

		assertEquals("/test/inner", resolved.getReadablePath());
		assertEquals("/me/drive/root:/test/inner", resolved.toApi());
		assertEquals("/me/drive/root:/test/inner", resolved.toASCIIApi());


		resolved = resolved.resolve("inner2");

		assertNull(resolved.getDriveId());
		assertNotNull(resolved.getReadablePath());

		assertEquals("/test/inner/inner2", resolved.getReadablePath());
		assertEquals("/me/drive/root:/test/inner/inner2", resolved.toApi());
		assertEquals("/me/drive/root:/test/inner/inner2", resolved.toASCIIApi());


		resolved = resolved.resolve("한글");

		assertNull(resolved.getDriveId());
		assertNotNull(resolved.getReadablePath());

		assertEquals("/test/inner/inner2/한글", resolved.getReadablePath());
		assertEquals("/me/drive/root:/test/inner/inner2/한글", resolved.toApi());
		assertEquals("/me/drive/root:/test/inner/inner2/%ED%95%9C%EA%B8%80", resolved.toASCIIApi());
	}
}