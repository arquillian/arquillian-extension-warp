package org.jboss.arquillian.warp.impl.client.operation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;

public class OperationalContexts {

    @Inject
    private Instance<ApplicationContext> applicationContextInst;

    @Inject
    private Instance<SuiteContext> suiteContextInst;

    @Inject
    private Instance<ClassContext> classContextInst;

    @Inject
    private Instance<TestContext> testContextInst;

    @Inject
    private Instance<WarpExecutionContext> warpExecutionContextInst;

    public OperationalContext application() {
        return new ApplicationOperationalContext();
    }

    public OperationalContext suite() {
        return new SuiteOperationalContext();
    }

    public OperationalContext clazz() {
        return new ClassOperationalContext();
    }

    public OperationalContext test() {
        return new TestOperationalContext();
    }

    public OperationalContext warp() {
        return new WarpOperationalContext();
    }

    private class ApplicationOperationalContext implements OperationalContext {
        private final ApplicationContext applicationContext = applicationContextInst.get();

        @Override
        public void activate() {
            applicationContext.activate();
        }

        @Override
        public void deactivate() {
            applicationContext.deactivate();
        }
    }

    private class SuiteOperationalContext extends ApplicationOperationalContext {
        private final SuiteContext suiteContext = suiteContextInst.get();

        @Override
        public void activate() {
            super.activate();
            suiteContext.activate();
        }

        @Override
        public void deactivate() {
            suiteContext.deactivate();
            super.deactivate();
        }
    }

    private class ClassOperationalContext extends SuiteOperationalContext {
        private final ClassContext classContext = classContextInst.get();
        private final Class<?> classContextId = classContext.getActiveId();

        @Override
        public void activate() {
            super.activate();
            classContext.activate(classContextId);
        }

        @Override
        public void deactivate() {
            classContext.deactivate();
            super.deactivate();
        }
    }

    private class TestOperationalContext extends ClassOperationalContext {
        private final TestContext testContext = testContextInst.get();
        private final Object testContextId = testContext.getActiveId();

        @Override
        public void activate() {
            super.activate();
            testContext.activate(testContextId);
        }

        @Override
        public void deactivate() {
            testContext.deactivate();
            super.deactivate();
        }
    }

    private class WarpOperationalContext extends TestOperationalContext {
        private final WarpExecutionContext warpContext = warpExecutionContextInst.get();

        @Override
        public void activate() {
            super.activate();
            warpContext.activate();
        }

        @Override
        public void deactivate() {
            warpContext.deactivate();
            super.deactivate();
        }
    }

}
