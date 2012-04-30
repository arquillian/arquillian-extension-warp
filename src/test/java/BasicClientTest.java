/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.jsfunitng.AssertionObject;
import org.jboss.arquillian.jsfunitng.ClientAction;
import org.jboss.arquillian.jsfunitng.MyBean;
import org.jboss.arquillian.jsfunitng.ServerAssertion;
import org.jboss.arquillian.jsfunitng.Warp;
import org.jboss.arquillian.jsfunitng.proxy.ProxyHolder;
import org.jboss.arquillian.jsfunitng.test.BeforeServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.seleniumemulation.WaitForPageToLoad;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

@RunWith(Arquillian.class)
@SuppressWarnings({ "serial", "unused" })
public class BasicClientTest {

    @Drone
    RemoteWebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "test.war").addClass(Servlet.class)
                .addClasses(AssertionObject.class, MyBean.class).addAsWebResource(new File("src/main/webapp/index.html"))
                .addClass(BasicClientTest.class).addClass(ServerAssertion.class)
                .addAsLibrary(Maven.withPom("pom.xml").dependency("commons-codec:commons-codec:1.6"))
                .addAsWebInfResource("beans.xml");
    }

    @Test
    @RunAsClient
    public void test() {

        Warp.execute(new ClientAction() {
            public void action() {
                browser.navigate().to(contextPath + "index.html");
            }
        }).verify(new InitialRequestAssertion());

        final WebElement sendAjax = new WebDriverWait(browser, 60).until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver input) {
                return browser.findElement(By.id("sendAjax"));
            }
        });

        Warp.execute(new ClientAction() {
            public void action() {
                sendAjax.click();
            }
        }).verify(new AjaxRequestAssertion());
    }

    public static class InitialRequestAssertion implements ServerAssertion {

        @BeforeServlet
        public void beforeServlet() {
            System.out.println("Hi server, here is my initial request!");
        }
    }

    public static class AjaxRequestAssertion implements ServerAssertion {

        @BeforeServlet
        public void beforeServlet() {
            System.out.println("Hi server, here is AJAX request!");
        }
    }

}
