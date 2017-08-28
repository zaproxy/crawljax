package com.crawljax.plugins.crawloverview;

import static com.crawljax.plugins.crawloverview.CandidateElementMatcher.element;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;

public class HoverTest {

	// NOTE: Assertion of elements' position and dimensions were done with:
	// - Chrome 59
	// - Firefox 55
	// other versions might fail (with some pixels up or down).

	private static final int MIN_HEIGHT = 500;

	private static final int MIN_WIDHT = 1024;

	private static final Logger LOG = LoggerFactory.getLogger(HoverTest.class);

	private static OutPutModel result;

	private static boolean resolutionBigEnough;

	@ClassRule
	public static final RunHoverCrawl HOVER_CRAWL = new RunHoverCrawl();

	@BeforeClass
	public static void runHoverTest() throws Exception {
		result = HOVER_CRAWL.getResult();
		resolutionBigEnough = calculateResolution(RunHoverCrawl.getOutDir());
	}

	private static boolean calculateResolution(File outFile) throws IOException {
		File indexScreensShot = new File(outFile, "screenshots/index.jpg");
		assertThat(indexScreensShot.exists(), is(true));
		BufferedImage img = ImageIO.read(indexScreensShot);
		boolean enough = img.getWidth() > MIN_WIDHT && img.getHeight() > MIN_HEIGHT;
		LOG.debug("Images size is {} x {}. Good enough = {}", img.getWidth(), img.getHeight(),
		        enough);
		return enough;
	}

	@Test
	public void verifyIndexHoversCorrect() {
		State state = result.getStates().get("index");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat("Number of hovers", candidates, hasSize(3));
		Assume.assumeTrue(resolutionBigEnough);

		switch (BrowserProvider.getBrowserType()) {
			case CHROME:
				assertThat(candidates,
				        hasItem(element(new Point(48, 113), new Dimension(52, 17))));
				assertThat(candidates,
				        hasItem(element(new Point(48, 131), new Dimension(51, 17))));
				assertThat(candidates,
				        hasItem(element(new Point(48, 149), new Dimension(200, 17))));
				break;
			case FIREFOX:
				assertThat(candidates,
				        hasItem(element(new Point(48, 116), new Dimension(61, 19))));
				assertThat(candidates,
				        hasItem(element(new Point(48, 135), new Dimension(61, 19))));
				assertThat(candidates,
				        hasItem(element(new Point(48, 154), new Dimension(252, 19))));
				break;
			default:
				// Don't assert for other browsers.
		}
	}

	@Test
	public void verifyPageAHoversCorrect() {
		State state = getStateByFileName("a.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(1));
		Assume.assumeTrue(resolutionBigEnough);

		switch (BrowserProvider.getBrowserType()) {
			case CHROME:
				assertThat(candidates,
				        hasItem(element(new Point(58, 142), new Dimension(89, 17))));
				break;
			case FIREFOX:
				assertThat(candidates,
				        hasItem(element(new Point(58, 145), new Dimension(110, 19))));
				break;
			default:
				// Don't assert for other browsers.
		}
	}

	@Test
	public void verifyPageBHoversCorrect() {
		State state = getStateByFileName("b.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(1));
		Assume.assumeTrue(resolutionBigEnough);

		switch (BrowserProvider.getBrowserType()) {
			case CHROME:
				assertThat(candidates,
				        hasItem(element(new Point(60, 163), new Dimension(51, 17))));
				break;
			case FIREFOX:
				assertThat(candidates,
				        hasItem(element(new Point(60, 166), new Dimension(61, 19))));
				break;
			default:
				// Don't assert for other browsers.
		}
	}

	@Test
	public void verifyPageCHoversCorrect() {
		State state = getStateByFileName("c.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(2));
		Assume.assumeTrue(resolutionBigEnough);

		switch (BrowserProvider.getBrowserType()) {
			case CHROME:
				assertThat(candidates,
				        hasItem(element(new Point(370, 160), new Dimension(51, 17))));
				assertThat(candidates,
				        hasItem(element(new Point(424, 160), new Dimension(76, 17))));
				break;
			case FIREFOX:
				assertThat(candidates,
				        hasItem(element(new Point(490, 164), new Dimension(61, 19))));
				assertThat(candidates,
				        hasItem(element(new Point(556, 164), new Dimension(93, 19))));
				break;
			default:
				// Don't assert for other browsers.
		}
	}

	private State getStateByFileName(String name) {
		for (State state : result.getStates().values()) {
			if (state.getUrl().endsWith(name)) {
				return state;
			}
		}
		fail("State with file name " + name + " wasn't found in " + result.getStates());
		return null;
	}

}
