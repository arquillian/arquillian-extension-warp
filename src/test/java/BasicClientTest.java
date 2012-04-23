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
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.javascript.JSInterfaceFactory;
import org.jboss.arquillian.graphene.page.interception.XhrInjection;
import org.jboss.arquillian.jsfunitng.AssertionObject;
import org.jboss.arquillian.jsfunitng.MyBean;
import org.jboss.arquillian.jsfunitng.Servlet;
import org.jboss.arquillian.jsfunitng.javascript.RequestEnrichment;
import org.jboss.arquillian.jsfunitng.utils.SerializationUtils;
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
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

@RunWith(Arquillian.class)
public class BasicClientTest {

    @Drone
    RemoteWebDriver browser;

    @ArquillianResource
    URL contextPath;
    
    XhrInjection xhrInjection = JSInterfaceFactory.create(XhrInjection.class).instantiate();
    RequestEnrichment enricher = JSInterfaceFactory.create(RequestEnrichment.class).instantiate();

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "test.war").addClass(Servlet.class)
                .addClasses(AssertionObject.class, SerializationUtils.class, MyBean.class)
                .addAsWebResource(new File("src/main/webapp/index.html"))
                .addAsLibrary(Maven.withPom("pom.xml").dependency("commons-codec:commons-codec"))
                .addAsWebInfResource("beans.xml");
    }

    
    @Test
    @RunAsClient
    public void test() {
        
        browser.navigate().to(contextPath + "index.html");

        AssertionObject assertionObject = new AssertionObject();
        assertionObject.setPayload("server");
        String requestEnrichment = SerializationUtils.serializeToBase64(assertionObject);

//        browser.executeScript("window.requestEnrichment = '" + requestEnrichment + "'");
        enricher.setRequestEnrichment(requestEnrichment);

//        WebElement enableInjection = browser.findElement(By.id("enableInjection"));
//      enableInjection.click();
        
        WebElement sendAjax = browser.findElement(By.id("sendAjax"));
        sendAjax.click();
        
        

//        String responseEnrichment = (String) browser.executeScript("return window.responseEnrichment");
        
        new WebDriverWait(browser, 600).until(new Predicate<WebDriver>() {
            
            @Override
            public boolean apply(WebDriver input) {
                return enricher.getResponseEnrichment() != null;
            }
        });
        
        String responseEnrichment = enricher.getResponseEnrichment();
//        if (!"null".equals(responseEnrichment)) {
            assertionObject = SerializationUtils.deserializeFromBase64(responseEnrichment);
            assertionObject.method();
//        }
    }
}
