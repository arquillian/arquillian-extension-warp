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
package org.jboss.as.quickstarts.spring;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.extension.servlet.AfterServlet;
import org.jboss.arquillian.warp.extension.spring.SpringMvcResult;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
@WarpTest
@RunWith(Arquillian.class)
public class WelcomeControllerTestCase {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        File[] libs = DependencyResolvers.use(MavenDependencyResolver.class)
                .loadEffectivePom("pom.xml")
                .artifacts("org.springframework:spring-webmvc:3.1.1.RELEASE")
                .resolveAsFiles();

        return ShrinkWrap.create(WebArchive.class, "spring-test.war").addClasses(WelcomeController.class)
                .addAsWebInfResource("WEB-INF/web.xml", "web.xml")
                .addAsWebInfResource("WEB-INF/welcome-servlet.xml", "welcome-servlet.xml")
                .addAsWebInfResource("WEB-INF/jsp/welcome.jsp", "jsp/welcome.jsp")
                .addAsLibraries(libs);
    }

    @Test
    @RunAsClient
    public void test() {
        Warp.execute(new ClientAction() {

            @Override
            public void action() {
                browser.navigate().to(contextPath + "welcome.do");
            }
        }).verify(new WelcomeControllerVerification());
    }

    public static class WelcomeControllerVerification extends ServerAssertion {

        private static final long serialVersionUID = 1L;

        @Inject
        private ModelAndView modelAndView;

        @AfterServlet
        public void testWelcome() {

            assertEquals("welcome", modelAndView.getViewName());
            assertEquals("Warp welcomes!", modelAndView.getModel().get("message"));
        }
    }
}
