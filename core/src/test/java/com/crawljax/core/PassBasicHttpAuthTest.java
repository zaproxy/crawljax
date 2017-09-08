package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class PassBasicHttpAuthTest {

	private static final String USERNAME = "test";
	private static final String PASSWORD = "test#&";
	private static final String USER_ROLE = "user";

	private Server server;
	private int port;

	@BeforeClass
	public static void setupBeforeClass() {
		// XXX PhantomJS issue: https://github.com/ariya/phantomjs/issues/12665
		// XXX JBrowserDriver issue:
		// https://github.com/MachinePublishers/jBrowserDriver/issues/132#issuecomment-259491450
		assumeThat("URLs with userinfo are not correctly processed",
		        BrowserProvider.getBrowserType(),
		        not(anyOf(is(EmbeddedBrowser.BrowserType.PHANTOMJS),
		                is(EmbeddedBrowser.BrowserType.JBD))));
	}

	@Before
	public void setup() throws Exception {
		server = new Server(0);
		ResourceHandler handler = new ResourceHandler();
		handler.setBaseResource(Resource.newClassPathResource("/site"));

		ConstraintSecurityHandler csh = newSecurityHandler(handler);

		server.setHandler(csh);
		server.start();

		this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

	}

	private ConstraintSecurityHandler newSecurityHandler(ResourceHandler handler) {
		HashLoginService login = new HashLoginService();
		login.setConfig(PassBasicHttpAuthTest.class.getResource("/realm.properties").getPath());

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		constraint.setRoles(new String[] { USER_ROLE });
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");

		ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
		csh.setAuthenticator(new BasicAuthenticator());
		csh.addConstraintMapping(cm);
		csh.setLoginService(login);
		csh.setHandler(handler);
		return csh;
	}

	@Test
	public void testProvidedCredentialsAreUsedInBasicAuth() throws Exception {
		String url = "http://localhost:" + port + "/infinite.html";
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(url);
		builder.setMaximumStates(3);
		builder.setBasicAuth(USERNAME, PASSWORD);
		builder.setBrowserConfig(
		        new BrowserConfiguration(BrowserProvider.getBrowserType()));
		CrawlSession session = new CrawljaxRunner(builder.build()).call();

		assertThat(session.getStateFlowGraph(), hasStates(3));
	}

	@After
	public void shutDown() throws Exception {
		server.stop();
	}

}
