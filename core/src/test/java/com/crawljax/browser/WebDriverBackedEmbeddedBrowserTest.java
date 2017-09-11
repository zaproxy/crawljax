package com.crawljax.browser;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.crawljax.core.CrawljaxException;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableSortedSet;

@Category(BrowserTest.class)
public class WebDriverBackedEmbeddedBrowserTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer(
			"/site/iframe");

	@Rule
	public final BrowserProvider provider = new BrowserProvider();

	@Test
	public void testGetDocument() throws Exception {
		// XXX JBrowserDriver issue: https://github.com/MachinePublishers/jBrowserDriver/issues/235
		assumeThat("iframe tests lead to hangs/loops", BrowserProvider.getBrowserType(),
		        is(not(EmbeddedBrowser.BrowserType.JBD)));

		WebDriverBackedEmbeddedBrowser browser = WebDriverBackedEmbeddedBrowser
				.withDriver(provider.newBrowser(),
						ImmutableSortedSet.<String> of(), 100, 100);

		Document doc;
		browser.goToUrl(SERVER.getSiteUrl());

		doc = DomUtils.asDocument(browser.getStrippedDom());
		NodeList frameNodes = doc.getElementsByTagName("IFRAME");
		assertEquals(2, frameNodes.getLength());

		doc = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
		frameNodes = doc.getElementsByTagName("IFRAME");
		assertEquals(2, frameNodes.getLength());

	}

	@Test
	public void saveScreenShot() throws CrawljaxException, IOException {
		WebDriverBackedEmbeddedBrowser browser = WebDriverBackedEmbeddedBrowser
				.withDriver(provider.newBrowser(),
						ImmutableSortedSet.<String> of(), 500, 500);

		File f = File.createTempFile("test-screenshot", ".png");
		f.deleteOnExit();

		browser.goToUrl(SERVER.getSiteUrl());
		browser.saveScreenShot(f);

		assertTrue(Files.size(f.toPath()) != 0);
	}
}
