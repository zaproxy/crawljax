package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import java.util.Objects;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Identification;
import com.google.common.base.MoreObjects;

/**
 * Conditions that returns true iff element found by given identification is visible.
 */
@Immutable
public class VisibleCondition implements Condition {

	private final Identification identification;

	/**
	 * @param identification
	 *            the identification.
	 */
	public VisibleCondition(Identification identification) {
		this.identification = identification;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return browser.isVisible(identification);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
		        .add("identification", identification)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), identification);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof VisibleCondition) {
			VisibleCondition that = (VisibleCondition) object;
			return Objects.equals(this.identification, that.identification);
		}
		return false;
	}

}
