package com.crawljax.core.largetests;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.Utils;

@Category(BrowserTest.class)
public class LargeFirefoxTest extends LargeTestBase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Utils.assumeBinary("webdriver.gecko.driver", "geckodriver");
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.FIREFOX, 1);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 200;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 200;
	}

}
