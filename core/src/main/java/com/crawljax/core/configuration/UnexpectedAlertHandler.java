package com.crawljax.core.configuration;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;

/**
 * A handler for unexpected/unhandled alerts ({@link UnhandledAlertException}).
 * <p>
 * Allows {@link com.crawljax.browser.WebDriverBackedEmbeddedBrowser WebDriverBackedEmbeddedBrowser}
 * to handle unexpected/unhandled alerts when trying to execute browser actions, to continue or not
 * with the normal crawling process.
 * 
 * @since 3.8
 */
@FunctionalInterface
public interface UnexpectedAlertHandler {

	/**
	 * Handles the unexpected/unhandled alert and tells whether or not the browser action should be
	 * retried.
	 * <p>
	 * Called when an {@code UnhandledAlertException} is caught after trying to execute a browser
	 * action.
	 * 
	 * @param browser
	 *            the browser that was executing the action.
	 * @param alertText
	 *            the text/message of the alert.
	 * @return {@code true} if the action should be retried, {@code false} otherwise.
	 */
	boolean handleAlert(WebDriver browser, String alertText);
}
