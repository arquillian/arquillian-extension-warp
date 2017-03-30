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

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.junit.Test;

public class TestMultiThreadEvents {

    static CountDownLatch latch;

    @Test
    public void testEventsAreDeliverableFromAnotherThread() throws Exception {
        ManagerBuilder builder = ManagerBuilder.from().extension(Observer.class);
        Manager manager = builder.create();
        manager.start();

        latch = new CountDownLatch(1);
        new Thread(new FiringRunnable(manager)).start();
        latch.await();

        manager.shutdown();
    }

    @Test
    public void testEventsAreDeliveredToAnotherThread() {
        ManagerBuilder builder = ManagerBuilder.from().extension(Observer.class);
        Manager manager = builder.create();
        manager.start();

        latch = new CountDownLatch(1);
    }

    public static class FiringRunnable implements Runnable {

        private Manager manager;

        public FiringRunnable(Manager manager) {
            super();
            this.manager = manager;
        }

        public void run() {
            manager.fire(new Event());
        }
    }

    public static class ConsumingRunnable implements Runnable {

        private Manager manager;

        public ConsumingRunnable(Manager manager) {
            this.manager = manager;
        }

        public void run() {
            manager.bind(ApplicationScoped.class, Observer.class, new Observer());
        }
    }

    public static class Observer {

        public void observer(@Observes Event event) {
            latch.countDown();
        }
    }

    public static class Event {
    }
}
