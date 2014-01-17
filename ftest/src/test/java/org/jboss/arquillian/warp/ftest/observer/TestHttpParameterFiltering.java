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
package org.jboss.arquillian.warp.ftest.observer;

import static org.hamcrest.Matchers.equalTo;
import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

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
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
public class TestHttpParameterFiltering {

    @Drone
    private WebDriver browser;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(TestingServlet.class)
                .addAsWebResource(new File("src/main/webapp/forms.html"));
    }

    @Test
    public void testGet() {
        browser.navigate().to(contextPath + "forms.html");

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.findElement(By.id("wrongButton")).click();
                    browser.findElement(By.id("getButton")).click();
                }
            })
            .observe(request().parameter().containsValue("getParameterName", "getParameterValue"))
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @ArquillianResource
                HttpServletRequest request;

                @BeforeServlet
                public void verifyRequestMethod() {
                    assertThat(request.getMethod(), equalTo("GET"));
                }
            });
    }

    @Test
    public void testPost() {
        browser.navigate().to(contextPath + "forms.html");

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.findElement(By.id("wrongButton")).click();
                    browser.findElement(By.id("postButton")).click();
                }
            })
            .observe(request().parameter().containsValue("postParameterName", "postParameterValue"))
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @ArquillianResource
                HttpServletRequest request;

                @BeforeServlet
                public void verifyRequestMethod() {
                    assertThat(request.getMethod(), equalTo("POST"));
                }
            });
    }
}
