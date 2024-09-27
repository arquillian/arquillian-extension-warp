/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.jsf.ftest.redirection;

import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;
import static org.jboss.arquillian.warp.jsf.Phase.RESTORE_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.jsf.AfterPhase;
import org.jboss.arquillian.warp.jsf.ftest.cdi.CdiBean;
import org.jboss.arquillian.warp.servlet.AfterServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@WarpTest
@RunAsClient
@RunWith(Arquillian.class)
public class TestJsfRedirect {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "jsf-test.war").addClasses(CdiBean.class)
            .addAsWebResource(new File("src/main/webapp/index.xhtml"))
            .addAsWebResource(new File("src/main/webapp/redirect.xhtml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
            .addAsWebResource(new File("src/main/webapp/templates/template.xhtml"), "templates/template.xhtml")
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"));
    }

    @Test
    public void test() {

        browser.navigate().to(contextPath + "redirect.jsf");

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.findElement(By.id("form:redirect")).click();
                }
            })
            .group("redirect.jsf")
            .observe(request().index(1))
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @ArquillianResource
                private HttpServletResponse response;

                @AfterPhase(RESTORE_VIEW)
                public void verify_that_the_appropriate_page_is_requested(@ArquillianResource FacesContext facesContext) {
                    assertEquals("/redirect.xhtml", facesContext.getViewRoot().getViewId());
                }

                @AfterServlet
                public void verify_that_response_is_redirected() {
                    assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
                    assertTrue(response.getHeader("Location").contains("index.jsf"));
                }
            })
            .group("index.jsf")
            .observe(request().index(2))
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @AfterPhase(RESTORE_VIEW)
                public void verify_that_the_appropriate_page_is_requested(@ArquillianResource FacesContext facesContext) {
                    assertEquals("/index.xhtml", facesContext.getViewRoot().getViewId());
                }
            })
            .execute();

        assertTrue(browser.getCurrentUrl().contains("index.jsf"));
    }
}
