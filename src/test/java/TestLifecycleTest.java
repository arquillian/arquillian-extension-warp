import java.util.List;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.jsfunitng.assertion.AssertionRegistry;
import org.jboss.arquillian.jsfunitng.lifecycle.BindLifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore.ObjectNotAssociatedException;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.jboss.arquillian.jsfunitng.test.BeforeServlet;
import org.jboss.arquillian.jsfunitng.test.BeforeServletEvent;
import org.jboss.arquillian.jsfunitng.test.LifecycleEvent;
import org.jboss.arquillian.jsfunitng.test.LifecycleTestDriver;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestLifecycleTest extends AbstractLifecycleTestBase {

    @Mock
    ServletRequest request;

    @Inject
    Instance<AssertionRegistry> registry;

    @Inject
    Instance<LifecycleManager> lifecycleManager;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(LifecycleManagerService.class);
        extensions.add(LifecycleTestDriver.class);
    }

    @Test
    public void test() throws ObjectNotAssociatedException {
        fire(new BeforeRequest(request));
        fire(new BindLifecycleManager<ServletRequest>(request, ServletRequest.class, request));

        TestingAssertion assertion = new TestingAssertion();
        registry.get().registerAssertion(assertion);

        LifecycleManager lifecycleManager = LifecycleManagerStore.get(ServletRequest.class, request);
        lifecycleManager.fireLifecycleEvent(new BeforeServletEvent());

        assertEventFired(BeforeServletEvent.class, 1);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 1);
        assertEventFired(Before.class, 1);
        assertEventFired(After.class, 1);
    }

    public static class TestingAssertion {

        @BeforeServlet
        public void assertion() {
        }
    }

}
