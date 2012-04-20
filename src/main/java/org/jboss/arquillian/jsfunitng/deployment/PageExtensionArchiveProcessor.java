package org.jboss.arquillian.jsfunitng.deployment;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.jsfunitng.filter.EnrichmentFilter;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class PageExtensionArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            WebArchive webArchive = (WebArchive) applicationArchive;

            // TODO should scan for page extensions or throw event which would collect them per extension
            webArchive.addAsWebResource("xhrInjection.js", "page-extensions/xhrInjection.js");
            webArchive.addAsWebResource("jsfunit.js", "page-extensions/jsfunit.js");
            webArchive.addAsWebResource("jquery.js", "page-extensions/jquery.js");
            webArchive.addAsWebResource("formSubmissionInterception.js", "page-extensions/formSubmissionInterception");
            webArchive.addPackage(EnrichmentFilter.class.getPackage());
        } else {
            // TODO user logger
            throw new IllegalStateException("applicationAchieve must be WebArchive");
        }
    }
}
