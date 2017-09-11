package com.crawljax.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.rules.ExternalResource;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

public class BrowserProvider extends ExternalResource {

	private static final String HEADLESS_BROWSER_ARG = "--headless";

	private static final Logger LOG = LoggerFactory
			.getLogger(BrowserProvider.class);
	private List<RemoteWebDriver> usedBrowsers;

	public static EmbeddedBrowser.BrowserType getBrowserType() {
		String browser = System.getProperty("test.browser");
		if (!Strings.isNullOrEmpty(browser)) {
			return EmbeddedBrowser.BrowserType.valueOf(browser);
		} else {
			return EmbeddedBrowser.BrowserType.FIREFOX;
		}
	}

	@Override
	protected void before() throws Throwable {
		usedBrowsers = new LinkedList<>();
	}

	public EmbeddedBrowser newEmbeddedBrowser() {
		return WebDriverBackedEmbeddedBrowser.withDriver(newBrowser());
	}

	/**
	 * First try to create phantomJS. If that fails, try Chrome.
	 */
	public RemoteWebDriver newBrowser() {
		RemoteWebDriver driver;
		switch (getBrowserType()) {
		case JBD:
			driver = new JBrowserDriver(Settings.builder().headless(true).build());
			break;
		case FIREFOX:
			driver = newFirefoxDriver();
			break;
		case INTERNET_EXPLORER:
			driver = new InternetExplorerDriver();
			break;
		case CHROME:
			driver = newChromeDriver();
			break;
		case PHANTOMJS:
			driver = new PhantomJSDriver();
			break;
		default:
			throw new IllegalStateException("Unsupported browsertype "
					+ getBrowserType());
		}

		/* Store the browser as a used browser so we can clean it up later. */
		usedBrowsers.add(driver);

		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
				.pageLoadTimeout(30, TimeUnit.SECONDS)
				.setScriptTimeout(30, TimeUnit.SECONDS);

		driver.manage().deleteAllCookies();

		return driver;
	}

	private static FirefoxDriver newFirefoxDriver() {
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments(HEADLESS_BROWSER_ARG);
		return new FirefoxDriver(options);
	}

	private static ChromeDriver newChromeDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments(HEADLESS_BROWSER_ARG);
		return new ChromeDriver(options);
	}

	@Override
	protected void after() {
		for (RemoteWebDriver browser : usedBrowsers) {
			try {
				browser.quit();
			} catch (RuntimeException e) {
				LOG.warn("Could not close the browser: {}", e.getMessage());
			}
		}
	}
}
