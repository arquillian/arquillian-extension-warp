/**
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
package org.jboss.arquillian.warp.ftest.failure;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.ftest.TestingServlet;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpRequestSpecifier;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author Lukas Fryc
 */
@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
public class TestActivityFailurePropagation {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addClass(TestingServlet.class)
                .addAsWebResource(new File("src/main/webapp/index.html"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test(expected = DefaultWarpRequestSpecifier.ActivityException.class)
    public void testAssertionErrorPropagation() {

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                    fail("AssertionError should be correctly handled and propagated to the client-side");
                }})
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void beforeServlet() {
                   System.out.println("Hi server, here is AJAX request!");
                }
            })
        ;
    }

    @Test(expected = DefaultWarpRequestSpecifier.ActivityException.class)
    public void testRuntimeExceptionPropagation() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                    throw new IllegalArgumentException();
                }})
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void beforeServlet() {
                    System.out.println("Hi server, here is AJAX request!");
                }
            })
        ;
    }

    @Test(expected = DefaultWarpRequestSpecifier.ActivityException.class)
    public void testExceptionPropagationBeforeAssertionErrorInInspection() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                    fail("Show me");
                }})
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void beforeServlet() {
                    fail("AssertionError should be ignored if there is an error on the client-side");
                }
            })
        ;
    }

    @Test(expected = RuntimeException.class)
    public void testExceptionPropagationIfExceptionInInspection() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }})
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void beforeServlet() {
                    throw new RuntimeException();
                }
            })
        ;
    }
}
