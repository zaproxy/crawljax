package com.crawljax.core.largetests;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.Utils;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class LargeChromeTest extends LargeTestBase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Utils.assumeBinary("webdriver.chrome.driver", "chromedriver");
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.CHROME);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 100;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 100;
	}
}
