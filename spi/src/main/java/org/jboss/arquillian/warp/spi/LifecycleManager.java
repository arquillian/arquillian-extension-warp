package org.jboss.arquillian.warp.spi;

public interface LifecycleManager {
    
    void fireLifecycleEvent(LifecycleEvent event);
}
