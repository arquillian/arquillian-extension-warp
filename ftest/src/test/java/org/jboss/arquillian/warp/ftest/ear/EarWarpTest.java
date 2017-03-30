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
package org.jboss.arquillian.warp.ftest.ear;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.jboss.arquillian.warp.servlet.AfterServlet;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * @author Lukas Fryc
 */
@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
public class EarWarpTest {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static EnterpriseArchive createDeployment() {

        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(
                        ShrinkWrap.create(WebArchive.class, "test.war")
                            .addClass(TestingServlet.class)
                            .addAsWebResource(new File("src/main/webapp/index.html"))
                            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"));
    }

    @Test
    public void test() {

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }
            })
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @ArquillianResource
                HttpServletRequest request;

                @ArquillianResource
                HttpServletResponse response;

                @BeforeServlet
                public void beforeServlet() {

                    System.out.println("Hi server, here is my initial request!");

                    assertNotNull("request must be enriched", request.getHeader(WarpCommons.ENRICHMENT_REQUEST));

                    assertNotNull("request context must be available", request);

                    assertNotNull("responses enrichment is set before servlet processing",
                            response.getHeader(WarpCommons.ENRICHMENT_RESPONSE));
                }

                @AfterServlet
                public void afterServlet() {

                    System.out.println("Servlet just processed my initial request!");

                    assertNotNull("responses enrichment is set before servlet processing",
                            response.getHeader(WarpCommons.ENRICHMENT_RESPONSE));

                    assertFalse("some headers has been already set", response.getHeaderNames().isEmpty());
                }
            });

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.findElement(By.id("sendAjax")).click();
                }
            })
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void beforeServlet() {
                    System.out.println("Hi server, here is AJAX request!");
                }
            });
    }
}
