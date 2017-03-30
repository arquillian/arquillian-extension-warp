/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.ftest.commandBus;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import static junit.framework.Assert.assertTrue;

/**
 *
 * @author Aris Tzoumas
 *
 */
@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
public class TestEventBusHttpRedirect {

    @Drone
    private WebDriver browser;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(RedirectFilter.class)
                .addAsWebResource(new File("src/main/webapp/index.html"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web-redirect.xml"), "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @Test
    public void testRedirectWorks() {
        // if test runs this means that redirect is supported in CommandEventBus
        assertTrue(true);
    }

}
