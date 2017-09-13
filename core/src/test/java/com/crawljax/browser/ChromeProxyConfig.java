package com.crawljax.browser;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.Utils;

@Category(BrowserTest.class)
public class ChromeProxyConfig {

	@Test
	public void chromeProxyConfig() throws Exception {
		Utils.assumeBinary("webdriver.chrome.driver", "chromedriver");

		CrawlSession crawl =
		        new BaseCrawler(Resource.newClassPathResource("/site"),
		                "simplelink/simplelink.html") {
			        @Override
			        public CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				        CrawljaxConfigurationBuilder builder =
				                super.newCrawlConfigurationBuilder();
				        builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME));
				        return builder;
			        }
		        }.crawl();
		assertThat(crawl.getStateFlowGraph(), hasStates(2));
	}
}
