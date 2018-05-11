package com.crawljax.core.largetests;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assume.assumeThat;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeIETest extends LargeTestBase {

	private static CrawlSession session;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assumeThat(System.getProperty("os.name").toLowerCase(), containsString("windows"));

		session = setup(new BrowserConfiguration(BrowserType.INTERNET_EXPLORER), 400, 400);
	}

	@Override
	protected CrawlSession getSession() {
		return session;
	}
}
