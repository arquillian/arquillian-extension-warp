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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit5.ArquillianExtension;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/**
 * @author atzoum
 * @author lfryc
 */
@ExtendWith(ArquillianExtension.class)
@WarpTest
@RunAsClient
public class TestContainerToClientChannel {

    @ArquillianResource
    private URL contextPath;

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
            .addClasses(TestCaseEnricher.DEPENDENCIES)
            .addAsWebResource(new File("src/main/webapp/index.html"))
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsServiceProvider(RemoteLoadableExtension.class.getName(), TestCaseEnricher.class.getName());
    }

    @Test
    public void testSuccessfulOperation() {
        Warp.initiate(pingUrl)
            .inspect(new InspectionPerformingSuccessfulOperation());

        assertTrue(SuccessfulCommand.executedStatic, "operation should be executed on the client");
    }

    @EnrichTestCase
    public static class InspectionPerformingSuccessfulOperation extends Inspection {
        private static final long serialVersionUID = 1L;

        @Inject
        private Instance<ServiceLoader> serviceLoader;

        @BeforeServlet
        public void verifyTheOperationSucceded() {
            CommandService remoteOperationService = serviceLoader.get().onlyOne(CommandService.class);

            SuccessfulCommand result = remoteOperationService.execute(new SuccessfulCommand());
            assertTrue(result.executedInstance, "should be executed on the client");
        }
    }

    public static final class SuccessfulCommand implements Command {
        private static final long serialVersionUID = 1L;

        public boolean executedInstance = false;
        public static boolean executedStatic = false;

        @Override
        public void perform() {
            executedInstance = true;
            executedStatic = true;
        }
    }

    @Test
    public void testFailedOperation() {
        try {
            Warp.initiate(pingUrl)
                .inspect(new InspectionPerformingFailedOperation());
            fail();
        } catch (IllegalStateException e) {
            if (!e.getMessage().equals("failed on the client")) {
                fail();
            }
        }

        assertTrue(FailedCommand.startedStatic, "should started on the client");
        assertFalse(FailedCommand.executedStatic, "the execution should fail on client");
    }

    @EnrichTestCase
    public static class InspectionPerformingFailedOperation extends Inspection {
        private static final long serialVersionUID = 1L;

        @Inject
        private Instance<ServiceLoader> serviceLoader;

        @BeforeServlet
        public void verifyTheOperationSucceded() {
            CommandService remoteOperationService = serviceLoader.get().onlyOne(CommandService.class);

            FailedCommand result = remoteOperationService.execute(new FailedCommand());
            assertTrue(result.startedInstance, "should started on the client");
            assertFalse(result.executedInstance, "the execution should fail on client");
        }
    }

    public static final class FailedCommand implements Command {
        private static final long serialVersionUID = 1L;

        public boolean startedInstance = false;
        public static boolean startedStatic = false;
        public boolean executedInstance = false;
        public static boolean executedStatic = false;

        @Override
        public void perform() {
            startedInstance = true;
            startedStatic = true;
            if (startedInstance) {
                throw new IllegalStateException("failed on the client");
            }
            executedInstance = true;
            executedStatic = true;
        }
    }
}
