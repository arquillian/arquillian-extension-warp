package org.jboss.arquillian.warp.impl.client.execution;

import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.junit.Test;

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
