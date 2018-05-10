package com.crawljax.core.largetests;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.Utils;

@Category(BrowserTest.class)
public class LargeFirefoxTest extends LargeTestBase {

	private static CrawlSession session;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Utils.assumeBinary("webdriver.gecko.driver", "geckodriver");

		session = setup(new BrowserConfiguration(BrowserType.FIREFOX, 1), 200, 200);
	}

	@Override
	protected CrawlSession getSession() {
		return session;
	}
}
