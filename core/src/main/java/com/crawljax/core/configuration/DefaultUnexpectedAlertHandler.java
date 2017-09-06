package com.crawljax.core.configuration;

import org.openqa.selenium.WebDriver;

/**
 * An {@link UnexpectedAlertHandler} that does not handle the alert and always retries the action.
 * <p>
 * Should be used only if the alerts are automatically handled (accepted or dismissed) by the
 * selected {@code WebDriver}, for example, through the capability
 * {@link org.openqa.selenium.remote.CapabilityType#UNHANDLED_PROMPT_BEHAVIOUR
 * CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR}.
 * 
 * @since 3.8
 * @see #INSTANCE
 */
public class DefaultUnexpectedAlertHandler implements UnexpectedAlertHandler {

	/**
	 * The instance of {@code DefaultUnexpectedAlertHandler}.
	 */
	public static final DefaultUnexpectedAlertHandler INSTANCE =
	        new DefaultUnexpectedAlertHandler();

	@Override
	public boolean handleAlert(WebDriver browser, String alertText) {
		return true;
	}

}
