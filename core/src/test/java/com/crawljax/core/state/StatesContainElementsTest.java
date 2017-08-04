package com.crawljax.core.state;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.eclipse.jetty.util.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class StatesContainElementsTest {

	private CrawlSession crawl;

	@Before
	public void setup() {
		crawl = new BaseCrawler(Resource.newClassPathResource("demo-site")) {
			@Override
			protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				return super.newCrawlConfigurationBuilder()
				        .setMaximumStates(2);
			}
		}.crawl();
	}

	@Test
	public void whenCrawledTheStateVertexesContainEvents() {
		Set<StateVertex> allStates = crawl.getStateFlowGraph().getAllStates();
		for (StateVertex stateVertex : allStates) {
			if ("index".equals(stateVertex.getName())) {
				assertThat(stateVertex.getCandidateElements(), is(not(empty())));
			}
		}
	}
}
