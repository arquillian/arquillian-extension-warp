import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.List;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.jsfunitng.lifecycle.BindLifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore.ObjectNotAssociatedException;
import org.jboss.arquillian.jsfunitng.lifecycle.UnbindLifecycleManager;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManagerBindingTestCase extends AbstractLifecycleTestBase {

    @Mock
    ServletRequest request;

    @Inject
    Instance<LifecycleManagerStore> store;

    @Inject
    Instance<LifecycleManager> lifecycleManager;

    AnotherClass anotherInstance = new AnotherClass();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(LifecycleManagerService.class);
    }

    @Before
    public void initialize() {
        fire(new BeforeRequest(request));
    }

    @Test
    public void test_bind_manager_to_request() {
        fire(new BindLifecycleManager<ServletRequest>(request, ServletRequest.class, request));
        try {
            LifecycleManager resolvedLifecycleManager = LifecycleManagerStore.get(ServletRequest.class, request);
            assertNotNull("lifecycle manager should be bound to request", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context", lifecycleManager.get(),
                    resolvedLifecycleManager);
        } catch (ObjectNotAssociatedException e) {
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void test_unbind_manager_from_request() {
        try {
            store.get().bind(ServletRequest.class, request);
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
        try {
            fire(new UnbindLifecycleManager<ServletRequest>(request, ServletRequest.class, request));

            LifecycleManagerStore.get(ServletRequest.class, request);
            fail("lifecycle manager should be unbound from request");
        } catch (ObjectNotAssociatedException e) {
            // expected exception
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
            } catch (ObjectNotAssociatedException e) {
                // that is okay, we are cleaning store
            }
        }
    }

    @Test
    public void test_bind_manager_to_request_and_another_class() {
        fire(new BindLifecycleManager<ServletRequest>(request, ServletRequest.class, request));
        fire(new BindLifecycleManager<AnotherClass>(request, AnotherClass.class, anotherInstance));
        try {
            // verify lifecycle manager for request
            LifecycleManager resolvedLifecycleManager = LifecycleManagerStore.get(ServletRequest.class, request);
            assertNotNull("lifecycle manager should be bound to request", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context", lifecycleManager.get(),
                    resolvedLifecycleManager);

            // verify lifecycle manager for another class
            resolvedLifecycleManager = LifecycleManagerStore.get(AnotherClass.class, anotherInstance);
            assertNotNull("lifecycle manager should be bound to another instance", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context", lifecycleManager.get(),
                    resolvedLifecycleManager);

        } catch (ObjectNotAssociatedException e) {
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
                store.get().unbind(AnotherClass.class, anotherInstance);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void test_unbind_manager_from_request_and_another_class() {
        try {
            store.get().bind(ServletRequest.class, request);
            store.get().bind(AnotherClass.class, anotherInstance);
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
        try {
            fire(new UnbindLifecycleManager<ServletRequest>(request, ServletRequest.class, request));
            fire(new UnbindLifecycleManager<AnotherClass>(request, AnotherClass.class, anotherInstance));

            try {
                LifecycleManagerStore.get(ServletRequest.class, request);
                fail("lifecycle manager should be unbound from request");
            } catch (ObjectNotAssociatedException e) {
                // expected condition
            }
            try {
                LifecycleManagerStore.get(AnotherClass.class, anotherInstance);
                fail("lifecycle manager should be unbound from request");
            } catch (ObjectNotAssociatedException e) {
                // expected condition

            }
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
                store.get().unbind(AnotherClass.class, anotherInstance);
            } catch (ObjectNotAssociatedException e) {
                // that is okay, we are cleaning store
            }
        }
    }

    public static class AnotherClass {
    }
}
