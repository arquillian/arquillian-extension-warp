import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.jsfunitng.request.AfterRequest;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.jboss.arquillian.jsfunitng.request.RequestContextHandler;
import org.jboss.arquillian.jsfunitng.request.RequestContextImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleManagerTest extends AbstractManagerTestBase {

    @Mock
    ServletRequest request;

    @Inject
    Instance<LifecycleManager> lifecycleManager;

    @Inject
    Instance<Injector> injector;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(LifecycleManagerService.class);
        extensions.add(RequestContextHandler.class);
    }
    
    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(RequestContextImpl.class);
    }

    @Test
    public void lifecycle_manager_should_be_initialized_before_request() {
        // having
        assertNull(lifecycleManager.get());

        // when
        fire(new BeforeRequest(request));

        // then
        assertNotNull("lifecycle manager should be initialized on BeforeRequest", lifecycleManager.get());
    }

    @Test
    public void lifecycle_manager_should_be_finalized_after_request() {
        // having
        // - lifecycle manager instantiated on before request
        fire(new BeforeRequest(request));

        // when
        fire(new AfterRequest(request));

        // then
        injector.get().inject(this);
        assertNull("lifecycle manager should be finalized on AfterRequest", lifecycleManager.get());
    }
}
