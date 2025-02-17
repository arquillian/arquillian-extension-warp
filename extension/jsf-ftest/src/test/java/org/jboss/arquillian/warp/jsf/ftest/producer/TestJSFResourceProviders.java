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
package org.jboss.arquillian.warp.jsf.ftest.producer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.faces.application.Application;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.render.RenderKit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.jsf.AfterPhase;
import org.jboss.arquillian.warp.jsf.BeforePhase;
import org.jboss.arquillian.warp.jsf.Phase;
import org.jboss.arquillian.warp.jsf.ftest.cdi.CdiBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ArquillianExtension.class)
@WarpTest
@RunAsClient
public class TestJSFResourceProviders {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "jsf-test.war")
            .addClasses(CdiBean.class)
            .addAsWebResource(new File("src/main/webapp/index.xhtml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
            .addAsWebResource(new File("src/main/webapp/templates/template.xhtml"), "templates/template.xhtml")
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"));
    }

    /**Exclude this test for TomEE as long as the HtmlUnit issue is not fixed,
     * see https://github.com/arquillian/arquillian-extension-warp/issues/242  */
    @BeforeAll
    public static void beforeClass() throws IOException, InterruptedException {
       String tomEEHome = (String) System.getProperty("tomee.home");
       Assumptions.assumeTrue(tomEEHome == null || tomEEHome.length() == 0);
    }

    @Test
    public void test() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.jsf");
                }
            })
            .inspect(new Inspection() {
                         private static final long serialVersionUID = 1L;

                         @ArquillianResource
                         Application application;

                         @ArquillianResource
                         ELContext elContext;

                         @ArquillianResource
                         ELResolver elResolver;

                         @ArquillianResource
                         ExceptionHandler exceptionHandler;

                         @ArquillianResource
                         ExpressionFactory expressionFactory;

                         @ArquillianResource
                         ExternalContext externalContext;

                         @ArquillianResource
                         FacesContext facesContext;

                         @ArquillianResource
                         Flash flash;

                         @ArquillianResource
                         NavigationHandler navigationHandler;

                         @ArquillianResource
                         PartialViewContext partialViewContext;

                         @ArquillianResource
                         ResourceHandler resourceHandler;

                         @ArquillianResource
                         StateManager stateManager;

                         @ArquillianResource
                         ViewHandler viewHandler;

                         @BeforePhase(Phase.RESTORE_VIEW)
                         public void verify_instances_before_view_restore() {
                             verifyAllInstancesPresent();
                         }

                         @AfterPhase(Phase.RESTORE_VIEW)
                         public void verify_instances_after_view_restore(@ArquillianResource RenderKit renderKit,
                             @ArquillianResource UIViewRoot viewRoot) {
                             verifyAllInstancesPresent();
                             assertNotNull(renderKit);
                             assertNotNull(viewRoot);
                         }

                         @AfterPhase(Phase.RENDER_RESPONSE)
                         public void verify_instances_after_rendering() {
                             verifyAllInstancesPresent();
                         }

                         private void verifyAllInstancesPresent() {
                             assertNotNull(application);
                             assertNotNull(elContext);
                             assertNotNull(elResolver);
                             assertNotNull(exceptionHandler);
                             assertNotNull(expressionFactory);
                             assertNotNull(externalContext);
                             assertNotNull(facesContext);
                             assertNotNull(flash);
                             assertNotNull(navigationHandler);
                             assertNotNull(partialViewContext);
                             assertNotNull(resourceHandler);
                             assertNotNull(stateManager);
                             assertNotNull(viewHandler);
                         }
                     }
            );
    }
}
