package org.jboss.arquillian.jsfunitng.lifecycle;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.jsfunitng.test.LifecycleEvent;

public class LifecycleManager {

    @Inject
    private Event<LifecycleEvent> lifecycleEvent;

    public void fireLifecycleEvent(LifecycleEvent event) {
        lifecycleEvent.fire(event);
    }
}
