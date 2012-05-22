/**
 JBoss, Home of Professional Open Source
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
import static org.jboss.arquillian.warp.extension.phaser.Phase.RENDER_RESPONSE;
import static org.jboss.arquillian.warp.extension.phaser.Phase.UPDATE_MODEL_VALUES;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

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
import org.jboss.as.quickstarts.jsf.MyBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@WarpTest
@RunWith(Arquillian.class)
public class PhaserBasicTest {

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
        Warp.execute(new ClientAction() {

            @Override
            public void action() {
                browser.navigate().to(contextPath + "index.jsf");
            }
        }).verify(new InitialRequestVerification());

        NameChangedToX x = Warp.execute(new ClientAction() {
            public void action() {
                WebElement nameInput = browser.findElement(By.id("helloWorldJsf:nameInput"));
                nameInput.sendKeys("X");
            }
        }).verify(new NameChangedToX());

        // verify Object was Deserialized with Server state
        Assert.assertNotNull(x.getUpdatedName());
    }

    public static class InitialRequestVerification implements ServerAssertion {

        private static final long serialVersionUID = 1L;

        @Inject
        MyBean myBean;

        @AfterPhase(RENDER_RESPONSE)
        public void initial_state_havent_changed_yet() {
            assertEquals("John", myBean.getName());
        }
    }

    public static class NameChangedToX implements ServerAssertion {

        private static final long serialVersionUID = 1L;

        @Inject
        MyBean myBean;

        private String updatedName;

        @BeforePhase(UPDATE_MODEL_VALUES)
        public void initial_state_havent_changed_yet() {
            assertEquals("John", myBean.getName());
        }

        @AfterPhase(UPDATE_MODEL_VALUES)
        public void changed_input_value_has_been_applied() {
            assertEquals("JohnX", myBean.getName());
            updatedName = myBean.getName();
        }

        public String getUpdatedName() {
            return updatedName;
        }
    }
}
