package org.jboss.arquillian.warp.ftest.integration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.ftest.TestingServlet;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
@RunAsClient
public class NonWarpTest {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(TestingServlet.class)
                .addAsWebResource(new File("src/main/webapp/index.html"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void when_test_is_not_annotated_as_warp_test_then_proxy_should_not_be_used() {

        // when
        browser.get(contextPath.toExternalForm());

        // then
        URL url = URLUtils.buildUrl(browser.getCurrentUrl());
        assertEquals(8080, url.getPort());
    }
}
