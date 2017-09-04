package com.crawljax.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for tests.
 */
public final class Utils {

	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	private Utils() {
	}

	public static void assumeBinary(String systemProperty, String binaryName)
	        throws Exception {
		assumeThat(System.getProperty(systemProperty) != null
		        || isOnClassPath(binaryName), is(true));
	}

	private static boolean isOnClassPath(String binaryName)
	        throws IOException, InterruptedException {
		try {
			if (!System.getProperty("os.name").startsWith("Windows")) {
				Process exec = Runtime.getRuntime().exec("which " + binaryName);
				boolean found = exec.waitFor() == 0;
				LOG.info("Found {} on the classpath = {}", binaryName, found);
				return found;
			}
		} catch (RuntimeException e) {
			LOG.info("Could not determine if {} is on the classpath: {}", binaryName,
			        e.getMessage());
		}
		return false;
	}
}