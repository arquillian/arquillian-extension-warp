package org.jboss.arquillian.warp.impl.client.operation;

public interface OperationalContext {

    void activate();

    void deactivate();
}
