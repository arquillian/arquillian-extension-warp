import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleManagerStoreTest extends AbstractManagerTestBase {

    @Inject
    Instance<LifecycleManagerStore> store;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(LifecycleManagerService.class);
    }

    @Test
    public void test() {
        assertNotNull("store should be initialized on manager start", store.get());
    }
}
