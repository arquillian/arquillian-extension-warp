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
package org.jboss.arquillian.warp.jsf.ftest.lifecycle;

import java.io.File;
import java.net.URL;

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
import org.jboss.arquillian.warp.jsf.BeforePhase;
import org.jboss.arquillian.warp.jsf.Phase;
import org.jboss.arquillian.warp.jsf.ftest.cdi.CdiBean;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

@WarpTest
@RunAsClient
@RunWith(Arquillian.class)
public class TestJsfLifecycle {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "jsf-test.war").addClasses(CdiBean.class)
                .addAsWebResource(new File("src/main/webapp/index.xhtml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
                .addAsWebResource(new File("src/main/webapp/templates/template.xhtml"), "templates/template.xhtml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"));
    }

    @Test
    public void test() {

        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.jsf");
                }})
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void beforeServlet() {
                }

                @BeforePhase(Phase.RESTORE_VIEW)
                public void beforeRestoreView() {
                }

                @AfterPhase(Phase.RESTORE_VIEW)
                public void afterRestoreView() {
                }

                @BeforePhase(Phase.RENDER_RESPONSE)
                public void beforeRenderResponse() {
                }

                @AfterPhase(Phase.RENDER_RESPONSE)
                public void afterRenderResponse() {
                }
            });

        Warp
            .initiate(new Activity() {
                public void perform() {
                    WebElement nameInput = browser.findElement(By.id("helloWorldJsf:nameInput"));
                    nameInput.sendKeys("X");
                    browser.findElement(By.tagName("body")).click();
                }
            })
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforePhase(Phase.RESTORE_VIEW)
                public void beforeRestoreView() {
                }

                @AfterPhase(Phase.RESTORE_VIEW)
                public void afterRestoreView() {
                }

                @BeforePhase(Phase.APPLY_REQUEST_VALUES)
                public void beforeApplyRequestValues() {
                }

                @AfterPhase(Phase.APPLY_REQUEST_VALUES)
                public void afterApplyRequestValues() {
                }

                @BeforePhase(Phase.PROCESS_VALIDATIONS)
                public void beforeProcessValidations() {
                }

                @AfterPhase(Phase.PROCESS_VALIDATIONS)
                public void afterProcessValidations() {
                }

                @BeforePhase(Phase.UPDATE_MODEL_VALUES)
                public void beforeUpdateModelValues() {
                }

                @AfterPhase(Phase.UPDATE_MODEL_VALUES)
                public void afterUpdateModelValues() {
                }

                @BeforePhase(Phase.INVOKE_APPLICATION)
                public void beforeInvokeApplication() {
                }

                @AfterPhase(Phase.INVOKE_APPLICATION)
                public void afterInvokeApplication() {
                }

                @BeforePhase(Phase.RENDER_RESPONSE)
                public void beforeRenderResponse() {
                }

                @AfterPhase(Phase.RENDER_RESPONSE)
                public void afterRenderResponse() {
                }
            });

        new WebDriverWait(browser, 5).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver browser) {
                WebElement output = browser.findElement(By.id("helloWorldJsf:output"));
                try {
                    return output.getText().contains("JohnX");
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            }
        });
    }
}
