package org.jboss.arquillian.warp.impl.client.execution;

import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

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
            System.out.println(event);
            latch.countDown();
        }
    }

    public static class Event {
    }
}
