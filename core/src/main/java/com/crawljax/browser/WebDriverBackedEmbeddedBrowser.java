package com.crawljax.browser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.AcceptAllFramesChecker;
import com.crawljax.core.configuration.DefaultUnexpectedAlertHandler;
import com.crawljax.core.configuration.IgnoreFrameChecker;
import com.crawljax.core.configuration.UnexpectedAlertHandler;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.crawljax.forms.RandomInputValueGenerator;
import com.crawljax.util.DomUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorHandler.UnknownServerException;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class WebDriverBackedEmbeddedBrowser implements EmbeddedBrowser {
	private static final Logger LOGGER = LoggerFactory
	        .getLogger(WebDriverBackedEmbeddedBrowser.class);

	private static final int BROWSER_CLOSE_TIMEOUT_SECS = 3;
	private static final int BROWSER_CLOSE_2ND_TIMEOUT_SECS = 2;
	private static ExecutorService closeBrowserExecutor;

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 * 
	 * @param hubUrl
	 *            Url of the server.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent, long crawlWaitReload) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl),
		        filterAttributes, crawlWaitEvent, crawlWaitReload);
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 * 
	 * @param hubUrl
	 *            Url of the server.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, IgnoreFrameChecker ignoreFrameChecker) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl),
		        filterAttributes, crawlWaitEvent, crawlWaitReload, ignoreFrameChecker);
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 * 
	 * @param hubUrl
	 *            Url of the server.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param unexpectedAlertHandler
	 *            the handler of unexpected alerts, if {@code null} it's used the
	 *            {@link DefaultUnexpectedAlertHandler}.
	 * @return The EmbeddedBrowser.
	 * @since 3.8
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, UnexpectedAlertHandler unexpectedAlertHandler) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl),
		        filterAttributes, crawlWaitEvent, crawlWaitReload, unexpectedAlertHandler);
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 * 
	 * @param hubUrl
	 *            Url of the server.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 * @param unexpectedAlertHandler
	 *            the handler of unexpected alerts, if {@code null} it's used the
	 *            {@link DefaultUnexpectedAlertHandler}.
	 * @return The EmbeddedBrowser.
	 * @since 3.8
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, IgnoreFrameChecker ignoreFrameChecker,
	        UnexpectedAlertHandler unexpectedAlertHandler) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl),
		        filterAttributes, crawlWaitEvent, crawlWaitReload, ignoreFrameChecker,
		        unexpectedAlertHandler);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent, long crawlWaitReload) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
		        crawlWaitReload);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, IgnoreFrameChecker ignoreFrameChecker) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
		        crawlWaitReload, ignoreFrameChecker);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param unexpectedAlertHandler
	 *            the handler of unexpected alerts, if {@code null} it's used the
	 *            {@link DefaultUnexpectedAlertHandler}.
	 * @return The EmbeddedBrowser.
	 * @since 3.8
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, UnexpectedAlertHandler unexpectedAlertHandler) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
		        crawlWaitReload, unexpectedAlertHandler);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 * @param unexpectedAlertHandler
	 *            the handler of unexpected alerts, if {@code null} it's used the
	 *            {@link DefaultUnexpectedAlertHandler}.
	 * @return The EmbeddedBrowser.
	 * @since 3.8
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, IgnoreFrameChecker ignoreFrameChecker,
	        UnexpectedAlertHandler unexpectedAlertHandler) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
		        crawlWaitReload, ignoreFrameChecker, unexpectedAlertHandler);
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 * 
	 * @param hubUrl
	 *            Url of the server.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl));
	}

	/**
	 * Private used static method for creation of a RemoteWebDriver. Taking care of the default
	 * Capabilities and using the HttpCommandExecutor.
	 * 
	 * @param hubUrl
	 *            the url of the hub to use.
	 * @return the RemoteWebDriver instance.
	 */
	private static RemoteWebDriver buildRemoteWebDriver(String hubUrl) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setPlatform(Platform.ANY);
		URL url;
		try {
			url = new URL(hubUrl);
		} catch (MalformedURLException e) {
			LOGGER.error("The given hub url of the remote server is malformed can not continue!",
			        e);
			return null;
		}
		HttpCommandExecutor executor = null;
		try {
			executor = new HttpCommandExecutor(url);
		} catch (Exception e) {
			// TODO Stefan; refactor this catch, this will definitely result in
			// NullPointers, why
			// not throw RuntimeExcption direct?
			LOGGER.error("Received unknown exception while creating the "
			        + "HttpCommandExecutor, can not continue!", e);
			return null;
		}
		return new RemoteWebDriver(executor, capabilities);
	}

	private final ImmutableSortedSet<String> filterAttributes;
	private final WebDriver browser;

	private long crawlWaitEvent;
	private long crawlWaitReload;
	private IgnoreFrameChecker ignoreFrameChecker = new AcceptAllFramesChecker();
	private UnexpectedAlertHandler unexpectedAlertHandler = DefaultUnexpectedAlertHandler.INSTANCE;

	/**
	 * Constructor without configuration values.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver) {
		this.browser = driver;
		filterAttributes = ImmutableSortedSet.of();
	}

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitReload, long crawlWaitEvent) {
		this.browser = driver;
		this.filterAttributes = Preconditions.checkNotNull(filterAttributes);
		this.crawlWaitEvent = crawlWaitEvent;
		this.crawlWaitReload = crawlWaitReload;
	}

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent, IgnoreFrameChecker ignoreFrameChecker) {
		this(driver, filterAttributes, crawlWaitReload, crawlWaitEvent);
		this.ignoreFrameChecker = ignoreFrameChecker;
	}

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param unexpectedAlertHandler
	 *            the handler of unexpected alerts, if {@code null} it's used the
	 *            {@link DefaultUnexpectedAlertHandler}.
	 * @since 3.8
	 */
	public WebDriverBackedEmbeddedBrowser(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
	        long crawlWaitReload, UnexpectedAlertHandler unexpectedAlertHandler) {
		this(driver, filterAttributes, crawlWaitReload, crawlWaitEvent);
		if (unexpectedAlertHandler != null) {
			this.unexpectedAlertHandler = unexpectedAlertHandler;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 * @param unexpectedAlertHandler
	 *            the handler of unexpected alerts, if {@code null} it's used the
	 *            {@link DefaultUnexpectedAlertHandler}.
	 * @since 3.8
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver,
	        ImmutableSortedSet<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent, IgnoreFrameChecker ignoreFrameChecker,
	        UnexpectedAlertHandler unexpectedAlertHandler) {
		this(driver, filterAttributes, crawlWaitReload, crawlWaitEvent, unexpectedAlertHandler);
		this.ignoreFrameChecker = ignoreFrameChecker;
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver) {
		return new WebDriverBackedEmbeddedBrowser(driver);
	}

	/**
	 * @param url
	 *            The URL.
	 */
	@Override
	public void goToUrl(URI url) {
		try {
			browser.navigate().to(url.toString());
			Thread.sleep(this.crawlWaitReload);
			handlePopups();
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return;
		} catch (InterruptedException e) {
			LOGGER.debug("goToUrl got interrupted while waiting for the page to be loaded", e);
			Thread.currentThread().interrupt();
			return;
		}
	}

	/**
	 * alert, prompt, and confirm behave as if the OK button is always clicked.
	 */
	private void handlePopups() {
		try {
			executeJavaScript("window.alert = function(msg){return true;};"
			        + "window.confirm = function(msg){return true;};"
			        + "window.prompt = function(msg){return true;};");
		} catch (CrawljaxException e) {
			LOGGER.error("Handling of PopUp windows failed", e);
		}
	}

	/**
	 * Fires the event and waits for a specified time.
	 * 
	 * @param webElement
	 *            the element to fire event on.
	 * @param eventable
	 *            The HTML event type (onclick, onmouseover, ...).
	 * @return true if firing event is successful.
	 * @throws InterruptedException
	 *             when interrupted during the wait.
	 */
	private boolean fireEventWait(WebElement webElement, Eventable eventable)
	        throws ElementNotVisibleException, InterruptedException {
		switch (eventable.getEventType()) {
			case click:
				try {
					webElement.click();
				} catch (ElementNotVisibleException e) {
					throw e;
				} catch (ElementNotInteractableException e) {
					String message = e.getMessage();
					if (message != null) {
						// HtmlUnitDriver throws ElementNotInteractableException instead of
						// ElementNotVisibleException for elements that are not visible.
						if (message.contains("visible")) {
							throw new ElementNotVisibleException(message, e);
						}

						// FirefoxDriver no longer throws ElementNotVisibleException for
						// elements that are not visible, while this might catch other
						// non-interactable cases it allows to access the hidden elements.
						if (message.contains("FirefoxDriver")) {
							throw new ElementNotVisibleException(message, e);
						}
					}
					return false;
				} catch (WebDriverException e) {
					throwIfConnectionException(e);
					return false;
				}
				break;
			case hover:
				LOGGER.info("Eventype hover called but this isnt implemented yet");
				break;
			default:
				LOGGER.info("EventType {} not supported in WebDriver.", eventable.getEventType());
				return false;
		}

		Thread.sleep(this.crawlWaitEvent);
		return true;
	}

	@Override
	public void close() {
		LOGGER.debug("Closing the browser...");
		// close browser and close every associated window.
		Future<?> closeTask = getCloseBrowserExecutor().submit(browser::quit);
		try {
			awaitForCloseTask(closeTask, BROWSER_CLOSE_TIMEOUT_SECS);
		} catch (InterruptedException e) {
			LOGGER.debug("Interrupted while waiting for the browser to close.");
			try {
				// Give some more time anyway, we want to wait for
				// the process to terminate before returning.
				awaitForCloseTask(closeTask, BROWSER_CLOSE_2ND_TIMEOUT_SECS);
			} catch (InterruptedException ignore) {
				LOGGER.debug("Interrupted, a 2nd time, while waiting for the browser to close.");
			}
			Thread.currentThread().interrupt();
		}
		LOGGER.debug("Browser closed: {}", closeTask.isDone());
	}

	private void awaitForCloseTask(Future<?> closeTask, int timeoutSeconds) throws InterruptedException {
		try {
			closeTask.get(timeoutSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			LOGGER.debug("Browser not closed after {} seconds.", timeoutSeconds);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof WebDriverException) {
				throw wrapWebDriverExceptionIfConnectionException((WebDriverException) cause);
			}
			LOGGER.error("An exception occurred while closing the browser:", cause);
		}
	}

	private static ExecutorService getCloseBrowserExecutor() {
		if (closeBrowserExecutor == null) {
			createCloseBrowserExecutor();
		}
		return closeBrowserExecutor;
	}

	private static synchronized void createCloseBrowserExecutor() {
		if (closeBrowserExecutor == null) {
			closeBrowserExecutor = Executors.newCachedThreadPool(new CloseBrowserThreadFactory());
		}
	}

	@Override
	@Deprecated
	public String getDom() {
		return getStrippedDom();
	}

	@Override
	public String getStrippedDom() {

		try {
			String dom = toUniformDOM(DomUtils.getDocumentToString(getDomTreeWithFrames()));
			LOGGER.trace(dom);
			return dom;
		} catch (WebDriverException | CrawljaxException e) {
			LOGGER.warn("Could not get the dom", e);
			return "";
		}
	}

	@Override
	public String getUnStrippedDom() {
		try {
			return executeWithAlertHandler(browser::getPageSource);
		} catch (Exception e) {
			throw new WebDriverException(e);
		}
	}

	/**
	 * @param html
	 *            The html string.
	 * @return uniform version of dom with predefined attributes stripped
	 */
	private String toUniformDOM(String html) {

		Pattern p =
		        Pattern.compile("<SCRIPT(.*?)</SCRIPT>", Pattern.DOTALL
		                | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		String htmlFormatted = m.replaceAll("");

		p = Pattern.compile("<\\?xml:(.*?)>");
		m = p.matcher(htmlFormatted);
		htmlFormatted = m.replaceAll("");

		htmlFormatted = filterAttributes(htmlFormatted);
		return htmlFormatted;
	}

	/**
	 * Filters attributes from the HTML string.
	 * 
	 * @param html
	 *            The HTML to filter.
	 * @return The filtered HTML string.
	 */
	private String filterAttributes(String html) {
		String filteredHtml = html;
		for (String attribute : this.filterAttributes) {
			String regex = "\\s" + attribute + "=\"[^\"]*\"";
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(html);
			filteredHtml = m.replaceAll("");
		}

		// XXX Stop removing empty style attributes once Marionette issue is fixed:
		// https://bugzilla.mozilla.org/show_bug.cgi?id=1448340
		// In new Firefox versions (>= 59) Marionette adds an empty style attribute to clicked
		// elements which incorrectly causes new crawling states.
		filteredHtml = filteredHtml.replaceAll("(?i)\\sstyle=\"\"", "");

		return filteredHtml;
	}

	@Override
	public void goBack() {
		try {
			browser.navigate().back();
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	/**
	 * @param identification
	 *            The identification object.
	 * @param text
	 *            The input.
	 * @return true if succeeds.
	 */
	@Override
	public boolean input(Identification identification, String text) {
		try {
			WebElement field = browser.findElement(identification.getWebDriverBy());
			if (field != null) {
				field.clear();
				field.sendKeys(text);

				return true;
			}
			return false;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * Fires an event on an element using its identification.
	 * 
	 * @param eventable
	 *            The eventable.
	 * @return true if it is able to fire the event successfully on the element.
	 * @throws InterruptedException
	 *             when interrupted during the wait.
	 */
	@Override
	public synchronized boolean fireEventAndWait(Eventable eventable)
	        throws ElementNotVisibleException,
	        NoSuchElementException, InterruptedException {
		try {

			boolean handleChanged = false;
			boolean result = false;

			if (eventable.getRelatedFrame() != null && !eventable.getRelatedFrame().equals("")) {
				LOGGER.debug("switching to frame: {}", eventable.getRelatedFrame());
				try {

					switchToFrame(eventable.getRelatedFrame());
				} catch (NoSuchFrameException e) {
					LOGGER.debug("Frame not found, possibily while back-tracking..", e);
					// TODO Stefan, This exception is catched to prevent stopping
					// from working
					// This was the case on the Gmail case; find out if not switching
					// (catching)
					// Results in good performance...
				}
				handleChanged = true;
			}

			WebElement webElement =
			        browser.findElement(eventable.getIdentification().getWebDriverBy());

			if (webElement != null) {
				result = fireEventWait(webElement, eventable);
			}

			if (handleChanged) {
				browser.switchTo().defaultContent();
			}
			return result;
		} catch (ElementNotVisibleException | NoSuchElementException e) {
			throw e;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * Execute JavaScript in the browser.
	 * 
	 * @param code
	 *            The code to execute.
	 * @return The return value of the JavaScript.
	 * @throws CrawljaxException
	 *             when javascript execution failed.
	 */
	@Override
	public Object executeJavaScript(String code) throws CrawljaxException {
		try {
			JavascriptExecutor js = (JavascriptExecutor) browser;
			return js.executeScript(code);
		} catch (UnsupportedOperationException e) {
			// HtmlUnitDriver throws UnsupportedOperationException if it can't execute the
			// JavaScript, for example, "Cannot execute JS against a plain text page".
			throw new CrawljaxException(e);
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			throw new CrawljaxException(e);
		}
	}

	/**
	 * Determines whether the corresponding element is visible.
	 * 
	 * @param identification
	 *            The element to search for.
	 * @return true if the element is visible
	 */
	@Override
	public boolean isVisible(Identification identification) {
		try {
			WebElement el = browser.findElement(identification.getWebDriverBy());
			if (el != null) {
				return el.isDisplayed();
			}

			return false;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * @return The current browser url.
	 */
	@Override
	public String getCurrentUrl() {
		try {
			return executeWithAlertHandler(browser::getCurrentUrl);
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		} catch (Exception e) {
			throw new WebDriverException(e);
		}
	}

	/**
	 * Executes the given callable handling {@code UnhandledAlertException}.
	 * <p>
	 * If an {@code UnhandledAlertException} is thrown it's caught and called the
	 * {@link #unexpectedAlertHandler}, the callable is executed again if permitted, otherwise the
	 * exception is re-thrown. An exception might still be thrown when trying to execute the action
	 * a second time.
	 *
	 * @param callable
	 *            the browser action to execute.
	 * @return the result of the action.
	 * @throws Exception
	 *             if an error occurred while executing the action.
	 */
	private <V> V executeWithAlertHandler(Callable<V> callable) throws Exception {
		try {
			return callable.call();
		} catch (UnhandledAlertException e) {
			LOGGER.debug("Got an unhandled alert: {}", e.getAlertText(), e);
			if (!unexpectedAlertHandler.handleAlert(browser, e.getAlertText())) {
				throw e;
			}
			return callable.call();
		}
	}

	@Override
	public void closeOtherWindows() {
		try {
			String current = browser.getWindowHandle();
			for (String handle : browser.getWindowHandles()) {
				if (!handle.equals(browser.getWindowHandle())) {
					browser.switchTo().window(handle);
					LOGGER.debug("Closing other window with title \"{}\"", browser.getTitle());
					browser.close();
					browser.switchTo().window(current);
				}
			}
		} catch (UnhandledAlertException e) {
			LOGGER.warn("While closing the window, an alert got ignored: {}", e.getMessage());
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	/**
	 * @return a Document object containing the contents of iframes as well.
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	private Document getDomTreeWithFrames() throws CrawljaxException {

		try {
			Document document = DomUtils.asDocument(getUnStrippedDom());
			appendFrameContent(document.getDocumentElement(), document, "");
			return document;
		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	private void appendFrameContent(Element orig, Document document, String topFrame) {

		NodeList frameNodes = orig.getElementsByTagName("IFRAME");

		List<Element> nodeList = new ArrayList<Element>();

		for (int i = 0; i < frameNodes.getLength(); i++) {
			Element frameElement = (Element) frameNodes.item(i);
			nodeList.add(frameElement);
		}

		// Added support for FRAMES
		frameNodes = orig.getElementsByTagName("FRAME");
		for (int i = 0; i < frameNodes.getLength(); i++) {
			Element frameElement = (Element) frameNodes.item(i);
			nodeList.add(frameElement);
		}

		for (int i = 0; i < nodeList.size(); i++) {
			try {
				locateFrameAndgetSource(document, topFrame, nodeList.get(i));
			} catch (UnknownServerException | NoSuchFrameException e) {
				LOGGER.warn("Could not add frame contents for element {}", nodeList.get(i));
				LOGGER.debug("Could not load frame because of {}", e.getMessage(), e);
			}
		}
	}

	private void locateFrameAndgetSource(Document document, String topFrame, Element frameElement)
	        throws NoSuchFrameException {
		String frameIdentification = "";

		if (topFrame != null && !topFrame.equals("")) {
			frameIdentification += topFrame + ".";
		}

		String nameId = DomUtils.getFrameIdentification(frameElement);

		if (nameId != null
		        && !ignoreFrameChecker.isFrameIgnored(frameIdentification + nameId)) {
			frameIdentification += nameId;

			String handle = browser.getWindowHandle();

			LOGGER.debug("The current H: {}", handle);

			switchToFrame(frameIdentification);

			String toAppend = getUnStrippedDom();

			LOGGER.debug("frame dom: {}", toAppend);

			browser.switchTo().defaultContent();

			try {
				Element toAppendElement = DomUtils.asDocument(toAppend).getDocumentElement();
				Element importedElement =
				        (Element) document.importNode(toAppendElement, true);
				frameElement.appendChild(importedElement);

				appendFrameContent(importedElement, document, frameIdentification);
			} catch (DOMException | IOException e) {
				LOGGER.info("Got exception while inspecting a frame: {} continuing...",
				        frameIdentification, e);
			}
		}
	}

	private void switchToFrame(String frameIdentification) throws NoSuchFrameException {
		LOGGER.debug("frame identification: {}", frameIdentification);

		if (frameIdentification.contains(".")) {
			String[] frames = frameIdentification.split("\\.");

			for (String frameId : frames) {
				LOGGER.debug("switching to frame: {}", frameId);
				browser.switchTo().frame(frameId);
			}

		} else {
			browser.switchTo().frame(frameIdentification);
		}

	}

	/**
	 * @return the dom without the iframe contents.
	 * @see com.crawljax.browser.EmbeddedBrowser#getStrippedDomWithoutIframeContent()
	 */
	@Override
	public String getStrippedDomWithoutIframeContent() {
		try {
			String dom = getUnStrippedDom();
			String result = toUniformDOM(dom);
			return result;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return "";
		}
	}

	/**
	 * @param input
	 *            the input to be filled.
	 * @return FormInput with random value assigned if possible. If no values were set it returns
	 *         <code>null</code>
	 */
	@Override
	public FormInput getInputWithRandomValue(FormInput input) {

		WebElement webElement;
		try {
			webElement = browser.findElement(input.getIdentification().getWebDriverBy());
			if (!webElement.isDisplayed()) {
				return null;
			}
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return null;
		}

		Set<InputValue> values = new HashSet<>();
		try {
			setRandomValues(input, webElement, values);
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return null;
		}
		if (values.isEmpty()) {
			return null;
		}
		input.setInputValues(values);
		return input;

	}

	private void setRandomValues(FormInput input, WebElement webElement, Set<InputValue> values) {
		String inputString = input.getType().toLowerCase();
		if (inputString.startsWith("text")) {
			values.add(new InputValue(new RandomInputValueGenerator()
			        .getRandomString(FormHandler.RANDOM_STRING_LENGTH), true));
		} else if (inputString.equals("checkbox") || inputString.equals("radio")
		        && !webElement.isSelected()) {
			if (new RandomInputValueGenerator().getCheck()) {
				values.add(new InputValue("1", true));
			} else {
				values.add(new InputValue("0", false));
			}
		} else if (inputString.equals("select")) {
			Select select = new Select(webElement);
			if (!select.getOptions().isEmpty()) {
				WebElement option =
				        new RandomInputValueGenerator().getRandomItem(select.getOptions());
				values.add(new InputValue(option.getText(), true));
			}

		}
	}

	@Override
	public String getFrameDom(String iframeIdentification) {
		try {

			switchToFrame(iframeIdentification);

			// make a copy of the dom before changing into the top page
			String frameDom = getUnStrippedDom();

			browser.switchTo().defaultContent();

			return frameDom;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return "";
		}
	}

	/**
	 * @param identification
	 *            the identification of the element.
	 * @return true if the element can be found in the DOM tree.
	 */
	@Override
	public boolean elementExists(Identification identification) {
		try {
			WebElement el = browser.findElement(identification.getWebDriverBy());
			// TODO Stefan; I think el will never be null as a
			// NoSuchElementExcpetion will be
			// thrown, catched below.
			return el != null;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * @param identification
	 *            the identification of the element.
	 * @return the found element.
	 */
	@Override
	public WebElement getWebElement(Identification identification) {
		try {
			return browser.findElement(identification.getWebDriverBy());
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	/**
	 * @return the period to wait after an event.
	 */
	protected long getCrawlWaitEvent() {
		return crawlWaitEvent;
	}

	/**
	 * @return the list of attributes to be filtered from DOM.
	 */
	protected ImmutableSortedSet<String> getFilterAttributes() {
		return filterAttributes;
	}

	/**
	 * @return the period to waint after a reload.
	 */
	protected long getCrawlWaitReload() {
		return crawlWaitReload;
	}

	@Override
	public void saveScreenShot(File file) throws CrawljaxException {
		try {
			File tmpfile = takeScreenShotOnBrowser(browser, OutputType.FILE);
			try {
				Files.copy(tmpfile, file);
			} catch (IOException e) {
				throw new CrawljaxException(e);
			}
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	private <T> T takeScreenShotOnBrowser(WebDriver driver, OutputType<T> outType)
	        throws CrawljaxException {
		if (driver instanceof TakesScreenshot) {
			T screenshot = ((TakesScreenshot) driver).getScreenshotAs(outType);
			removeCanvasGeneratedByFirefoxDriverForScreenshots();
			return screenshot;
		} else if (driver instanceof RemoteWebDriver) {
			WebDriver augmentedWebdriver = new Augmenter().augment(driver);
			return takeScreenShotOnBrowser(augmentedWebdriver, outType);
		} else if (driver instanceof WrapsDriver) {
			return takeScreenShotOnBrowser(((WrapsDriver) driver).getWrappedDriver(), outType);
		} else {
			throw new CrawljaxException("Your current WebDriver doesn't support screenshots.");
		}
	}

	@Override
	public byte[] getScreenShot() throws CrawljaxException {
		try {
			return takeScreenShotOnBrowser(browser, OutputType.BYTES);
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	private void removeCanvasGeneratedByFirefoxDriverForScreenshots() {
		String js = "";
		js += "var canvas = document.getElementById('fxdriver-screenshot-canvas');";
		js += "if(canvas != null){";
		js += "canvas.parentNode.removeChild(canvas);";
		js += "}";
		try {
			executeJavaScript(js);
		} catch (CrawljaxException e) {
			LOGGER.error("Removing of Canvas Generated By FirefoxDriver failed,"
			        + " most likely leaving it in the browser", e);
		}
	}

	/**
	 * @return the WebDriver used as an EmbeddedBrowser.
	 */
	public WebDriver getBrowser() {
		return browser;
	}

	private boolean exceptionIsConnectionException(WebDriverException exception) {
		return exception != null && exception.getCause() != null
		        && exception.getCause() instanceof IOException;
	}

	private RuntimeException wrapWebDriverExceptionIfConnectionException(
	        WebDriverException exception) {
		if (exceptionIsConnectionException(exception)) {
			return new BrowserConnectionException(exception);
		}
		return exception;
	}

	private void throwIfConnectionException(WebDriverException exception) {
		if (exceptionIsConnectionException(exception)) {
			throw wrapWebDriverExceptionIfConnectionException(exception);
		}
	}

	private static class CloseBrowserThreadFactory implements ThreadFactory {
		private static final String NAME_PREFIX = "Crawljax-CloseBrowserThread-";
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);

		public CloseBrowserThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
			        NAME_PREFIX + threadNumber.getAndIncrement(),
			        0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
