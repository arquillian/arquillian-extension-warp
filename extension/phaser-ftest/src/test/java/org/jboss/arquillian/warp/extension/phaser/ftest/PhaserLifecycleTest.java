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
package org.jboss.arquillian.warp.extension.phaser.ftest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;

import javax.faces.context.FacesContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.extension.phaser.AfterPhase;
import org.jboss.arquillian.warp.extension.phaser.BeforePhase;
import org.jboss.arquillian.warp.extension.phaser.Phase;
import org.jboss.arquillian.warp.extension.servlet.AfterServlet;
import org.jboss.arquillian.warp.extension.servlet.BeforeServlet;
import org.jboss.as.quickstarts.jsf.MyBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@WarpTest
@RunWith(Arquillian.class)
public class PhaserLifecycleTest {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "jsf-test.war").addClasses(MyBean.class)
                .addAsWebResource(new File("src/main/webapp/index.xhtml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
                .addAsWebResource(new File("src/main/webapp/templates/template.xhtml"), "templates/template.xhtml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"));
    }

    @Test
    @RunAsClient
    public void test() {
        ExecuteAllPhases executed = Warp.execute(new ClientAction() {
            public void action() {
                browser.navigate().to(contextPath + "index.jsf");
            }
        }).verify(new ExecuteAllPhases());

        assertFalse(executed.isPostback());
        verifyExecutedPhases(executed);

        executed = Warp.execute(new ClientAction() {
            public void action() {
                WebElement nameInput = browser.findElement(By.id("helloWorldJsf:nameInput"));
                nameInput.sendKeys("X");
            }
        }).verify(new ExecuteAllPhases());

        assertTrue(executed.isPostback());
        verifyExecutedPhases(executed);
    }

    private static void verifyExecutedPhases(ExecuteAllPhases executed) {
        EnumSet<Phase> expectedPhases = executed.isPostback() ? EnumSet.allOf(Phase.class) : EnumSet.of(Phase.RESTORE_VIEW,
                Phase.RENDER_RESPONSE);
        assertEquals("before", expectedPhases, executed.getBefore());
        assertEquals("after", expectedPhases, executed.getAfter());
    }

    public static class ExecuteAllPhases extends ServerAssertion {

        private static final long serialVersionUID = 1L;

        private EnumSet<Phase> before = EnumSet.noneOf(Phase.class);
        private EnumSet<Phase> after = EnumSet.noneOf(Phase.class);
        private boolean postback = false;

        @BeforeServlet
        public void beforeServlet() {
            assertEquals("before", EnumSet.noneOf(Phase.class), before);
            assertEquals("after", EnumSet.noneOf(Phase.class), after);
        }

        @BeforePhase(Phase.RESTORE_VIEW)
        public void beforeRestoreView() {
            before.add(Phase.RESTORE_VIEW);
        }

        @AfterPhase(Phase.RESTORE_VIEW)
        public void afterRestoreView() {
            after.add(Phase.RESTORE_VIEW);
        }

        @BeforePhase(Phase.APPLY_REQUEST_VALUES)
        public void beforeApplyRequestValues() {
            before.add(Phase.APPLY_REQUEST_VALUES);
        }

        @AfterPhase(Phase.APPLY_REQUEST_VALUES)
        public void afterApplyRequestValues() {
            after.add(Phase.APPLY_REQUEST_VALUES);
        }

        @BeforePhase(Phase.PROCESS_VALIDATIONS)
        public void beforeProcessValidations() {
            before.add(Phase.PROCESS_VALIDATIONS);
        }

        @AfterPhase(Phase.PROCESS_VALIDATIONS)
        public void afterProcessValidations() {
            after.add(Phase.PROCESS_VALIDATIONS);
        }

        @BeforePhase(Phase.UPDATE_MODEL_VALUES)
        public void beforeUpdateModelValues() {
            before.add(Phase.UPDATE_MODEL_VALUES);
        }

        @AfterPhase(Phase.UPDATE_MODEL_VALUES)
        public void afterUpdateModelValues() {
            after.add(Phase.UPDATE_MODEL_VALUES);
        }

        @BeforePhase(Phase.INVOKE_APPLICATION)
        public void beforeInvokeApplication() {
            before.add(Phase.INVOKE_APPLICATION);
        }

        @AfterPhase(Phase.INVOKE_APPLICATION)
        public void afterInvokeApplication() {
            after.add(Phase.INVOKE_APPLICATION);
        }

        @BeforePhase(Phase.RENDER_RESPONSE)
        public void beforeRenderResponse() {
            before.add(Phase.RENDER_RESPONSE);
        }

        @AfterPhase(Phase.RENDER_RESPONSE)
        public void afterRenderResponse() {
            after.add(Phase.RENDER_RESPONSE);

            postback = FacesContext.getCurrentInstance().isPostback();
        }

        @AfterServlet
        public void afterServlet() {
            verifyExecutedPhases(this);
        }

        public EnumSet<Phase> getBefore() {
            return before;
        }

        public EnumSet<Phase> getAfter() {
            return after;
        }

        public boolean isPostback() {
            return postback;
        }
    }
}
