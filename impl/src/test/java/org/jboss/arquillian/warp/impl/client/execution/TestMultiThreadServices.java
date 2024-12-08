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
package org.jboss.arquillian.warp.impl.client.execution;

import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.junit.jupiter.api.Test;

public class TestMultiThreadServices {

    static CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void testServiceCanBeInvokedFromAnotherThread() throws Exception {
        ManagerBuilder builder = ManagerBuilder.from();
        Manager manager = builder.create();
        manager.start();

        manager.bind(ApplicationScoped.class, Component.class, new Component());

        Service service = new ServiceImpl();
        manager.inject(service);

        new Thread(new ConsumingRunnable(service)).start();

        latch.await();
        manager.shutdown();
    }

    public static class ConsumingRunnable implements Runnable {

        private Service service;

        public ConsumingRunnable(Service service) {
            this.service = service;
        }

        public void run() {
            service.invokeService();
        }
    }

    public interface Service {
        void invokeService();
    }

    public class ServiceImpl implements Service {

        @Inject
        Instance<Component> component;

        @Override
        public void invokeService() {
            latch.countDown();
        }
    }

    public static class Component {
    }
}
