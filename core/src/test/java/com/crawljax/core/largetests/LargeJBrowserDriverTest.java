package com.crawljax.core.largetests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeJBrowserDriverTest extends LargeTestBase {

	private static CrawlSession session;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assumeThat("not able to run without some com.sun classes", hasComSunClasses(), is(true));
		session = setup(new BrowserConfiguration(BrowserType.JBD), 200, 200);
	}

	private static boolean hasComSunClasses() {
		try {
			Class.forName("com.sun.webkit.network.CookieManager");
			return true;
		} catch (ClassNotFoundException e) {
		}
		return false;
	}

	@Override
	protected CrawlSession getSession() {
		return session;
	}

}
