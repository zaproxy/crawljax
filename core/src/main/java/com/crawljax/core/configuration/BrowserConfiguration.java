package com.crawljax.core.configuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;
import javax.inject.Provider;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

@Immutable
public class BrowserConfiguration {

	/**
	 * The total number of retries when a browser can not be created.
	 */
	public static final int BROWSER_START_RETRIES = 2;

	/**
	 * The number of milliseconds to sleep when a browser can not be created.
	 */
	public static final long BROWSER_SLEEP_FAILURE = TimeUnit.SECONDS.toMillis(10);

	private static final Provider<EmbeddedBrowser> DEFAULT_BROWSER_BUILDER =
	        new Provider<EmbeddedBrowser>() {

		        @Override
		        public EmbeddedBrowser get() {
			        throw new IllegalStateException(
			                "This is just a placeholder and should not be called");
		        }

		        @Override
		        public String toString() {
			        return "Default webdriver factory";
		        };

	        };

	private final BrowserType browsertype;
	private final int numberOfBrowsers;
	private final Provider<EmbeddedBrowser> browserBuilder;
	private String remoteHubUrl;
	private String lang;
	private boolean headless;

	/**
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param remoteUrl
	 *            the URL of the remote HUB
	 */
	public static BrowserConfiguration remoteConfig(int numberOfBrowsers, String remoteUrl) {
		BrowserConfiguration config =
		        new BrowserConfiguration(BrowserType.REMOTE, numberOfBrowsers);
		config.remoteHubUrl = remoteUrl;
		return config;
	}

	/**
	 * This configuration will start one browser of the selected type.
	 * 
	 * @param browsertype
	 *            The browser you would like to run.
	 */
	public BrowserConfiguration(BrowserType browsertype) {
		this(browsertype, 1);
	}

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 */
	public BrowserConfiguration(BrowserType browsertype, int numberOfBrowsers) {
		this(browsertype, numberOfBrowsers, DEFAULT_BROWSER_BUILDER);
	}

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param builder
	 *            a custom {@link WebDriverBrowserBuilder}.
	 */
	public BrowserConfiguration(BrowserType browsertype, int numberOfBrowsers,
	        Provider<EmbeddedBrowser> builder) {
		Preconditions.checkArgument(numberOfBrowsers > 0,
		        "Number of browsers should be 1 or more");
		Preconditions.checkNotNull(browsertype);
		Preconditions.checkNotNull(builder);

		this.browsertype = browsertype;
		this.numberOfBrowsers = numberOfBrowsers;
		this.browserBuilder = builder;
		this.headless = true;
	}

	public BrowserType getBrowsertype() {
		return browsertype;
	}

	public int getNumberOfBrowsers() {
		return numberOfBrowsers;
	}

	public Provider<EmbeddedBrowser> getBrowserBuilder() {
		return browserBuilder;
	}

	public String getRemoteHubUrl() {
		return remoteHubUrl;
	}

	public boolean isDefaultBuilder() {
		return browserBuilder.equals(DEFAULT_BROWSER_BUILDER);
	}

	/**
	 * Tells whether or not the chosen browser should be started in headless mode.
	 * <p>
	 * This should be considered a hint, not all browsers support this feature (nor all browsers
	 * have a GUI).
	 * <p>
	 * The default is {@code true}.
	 * 
	 * @return {@code true} if the browser should be started in headless mode, {@code false}
	 *         otherwise.
	 * @since 3.8
	 * @see #setHeadless(boolean)
	 */
	public boolean isHeadless() {
		return headless;
	}

	/**
	 * Sets whether or not the chosen browser should be started in headless mode.
	 * <p>
	 * This should be considered a hint, not all browsers support this feature (nor all browsers
	 * have a GUI).
	 *
	 * @param headless
	 *            {@code true} if the browser should be started in headless mode, {@code false}
	 *            otherwise.
	 * @since 3.8
	 * @see #isHeadless()
	 */
	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
		        .add("browsertype", browsertype)
		        .add("numberOfBrowsers", numberOfBrowsers)
		        .add("browserBuilder", browserBuilder)
		        .add("remoteHubUrl", remoteHubUrl)
		        .add("language", lang)
		        .add("headless", headless)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(browsertype, numberOfBrowsers, browserBuilder,
		        remoteHubUrl, lang, headless);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof BrowserConfiguration) {
			BrowserConfiguration that = (BrowserConfiguration) object;
			return Objects.equals(this.browsertype, that.browsertype)
			        && Objects.equals(this.numberOfBrowsers, that.numberOfBrowsers)
			        && Objects.equals(this.browserBuilder, that.browserBuilder)
			        && Objects.equals(this.remoteHubUrl, that.remoteHubUrl)
			        && Objects.equals(this.lang, that.lang)
			        && Objects.equals(this.headless, that.headless);
		}
		return false;
	}

	/**
	 * @return the language header setting or <code>null</code> if not set.
	 */
	public String getLangOrNull() {
		return lang;
	}

	/**
	 * @param lang
	 *            the language header in http requests
	 */
	public void setLang(String lang) {
		Preconditions.checkNotNull(lang, "The language cannot be null");
		this.lang = lang;
	}

}
