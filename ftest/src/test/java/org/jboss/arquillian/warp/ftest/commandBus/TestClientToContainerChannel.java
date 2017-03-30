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
package org.jboss.arquillian.warp.ftest.commandBus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.impl.shared.command.Command;
import org.jboss.arquillian.warp.impl.shared.command.CommandService;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author atzoum
 *
 */
@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
@EnrichTestCase
public class TestClientToContainerChannel {

    @ArquillianResource
    private URL contextPath;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Drone
    private WebDriver browser;

    private Activity pingUrl = new Activity() {
        @Override
        public void perform() {
            browser.navigate().to(contextPath + "index.html");
        }
    };

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebResource(new File("src/main/webapp/index.html")).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(SuccessfulCommand.class);
    }

    @Test
    public void testSuccessfulOperation() {

        CommandService remoteOperationService = serviceLoader.get().onlyOne(CommandService.class);

        remoteOperationService.execute(new SuccessfulCommand());

        Warp
            .initiate(pingUrl)
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void verifyTheOperationSucceded() {
                    assertTrue(SuccessfulCommand.executed);
                }
            });
    }

    public static final class SuccessfulCommand implements Command {
        private static final long serialVersionUID = 1L;

        public static boolean executed = false;

        @Override
        public void perform() {
            executed = true;
        }
    }

    @Test
    public void testFailedOperation() {

        CommandService remoteOperationService = serviceLoader.get().onlyOne(CommandService.class);

        try {
            remoteOperationService.execute(new FailingCommand());
            fail("should fail on a server");
        } catch (IllegalStateException e) {
            if (!e.getMessage().equals("failed on the server")) {
                fail("unknown failure - expected to fail on the server");
            }
        }

        Warp
            .initiate(pingUrl)
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;

                @BeforeServlet
                public void verifyTheOperationSucceded() {
                    assertTrue(FailingCommand.started);
                    assertFalse(FailingCommand.executed);
                }
            });
    }

    public static final class FailingCommand implements Command {
        private static final long serialVersionUID = 1L;

        public static boolean started = false;
        public static boolean executed = false;

        @Override
        public void perform() {
            started = true;
            if (started) {
                throw new IllegalStateException("failed on the server");
            }
            executed = true;
        }
    }
}
