package com.crawljax.crawls;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.Utils;

@Category(BrowserTest.class)
public class CrawlConcurrently {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Utils.assumeBinary("webdriver.chrome.driver", "chromedriver");
		Utils.assumeBinary("webdriver.gecko.driver", "geckodriver");
	}

	@Test
	public void canRunMultipleCrawlersAtTheSameTime() throws Exception {
		List<Callable<CrawlSession>> tasks = new ArrayList<>();

		BaseCrawler c1 = createCrawler("concurrentcrawl1", EmbeddedBrowser.BrowserType.FIREFOX);
		tasks.add(c1::crawl);

		BaseCrawler c2 = createCrawler("concurrentcrawl2", EmbeddedBrowser.BrowserType.CHROME);
		tasks.add(c2::crawl);

		List<Future<CrawlSession>> results = Executors.newFixedThreadPool(2).invokeAll(tasks);

		URI siteUrl = c1.getWebServer().getSiteUrl();
		assertCrawledUrls(results.get(0).get(),
		        siteUrl + "concurrentcrawl1",
		        siteUrl + "concurrentcrawl1/page_a.html",
		        siteUrl + "concurrentcrawl1/page_b.html",
		        siteUrl + "concurrentcrawl1/page_c.html");

		siteUrl = c2.getWebServer().getSiteUrl();
		assertCrawledUrls(results.get(1).get(),
		        siteUrl + "concurrentcrawl2",
		        siteUrl + "concurrentcrawl2/page_x.html",
		        siteUrl + "concurrentcrawl2/page_y.html",
		        siteUrl + "concurrentcrawl2/page_z.html");
	}

	private BaseCrawler createCrawler(String siteExtension, EmbeddedBrowser.BrowserType browser) {
		return new BaseCrawler(siteExtension) {

			@Override
			public CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				return super.newCrawlConfigurationBuilder()
				        .setBrowserConfig(new BrowserConfiguration(browser));
			}
		};
	}

	private void assertCrawledUrls(CrawlSession crawlSession, String... urls) {
		List<String> crawledUrls = new ArrayList<>();
		for (StateVertex state : crawlSession.getStateFlowGraph().getAllStates()) {
			crawledUrls.add(state.getUrl());
		}

		assertThat(crawledUrls, hasItems(urls));
		assertThat(crawledUrls.size(), is(urls.length));
	}

}
