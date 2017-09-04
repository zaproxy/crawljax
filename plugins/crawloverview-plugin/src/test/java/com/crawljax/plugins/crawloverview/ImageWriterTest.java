package com.crawljax.plugins.crawloverview;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class ImageWriterTest {

	private static final String THUMB_HASH = "4040cc9bb7aaea0eebdc2879055eb68b";
	private static final String FULL_HASH = "84254c0309e738b13dc3968414dcae5a";

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void makingAThumbnailDoesntThrowException() throws Exception {
		URI file = OutputBuilderTest.class.getResource("/screenshot.png").toURI();
		File screenShot = new File(file);
		File fullScreenShot = folder.newFile();
		File thumbnail = folder.newFile();
		ImageWriter.writeScreenShotAndThumbnail(Files.toByteArray(screenShot), fullScreenShot,
		        thumbnail);

		assertThat("Thumbnail exists", thumbnail.exists(), is(true));
		String hash = Files.asByteSource(thumbnail).hash(Hashing.murmur3_128()).toString();
		assertThat("Thumb hash doesn't match", hash, is(THUMB_HASH));

		assertThat("Screenshot exists", fullScreenShot.exists(), is(true));
		hash = Files.asByteSource(fullScreenShot).hash(Hashing.murmur3_128()).toString();
		assertThat("Screenshot hash doesn't match", hash, is(FULL_HASH));
	}
}
