package org.jboss.arquillian.warp.impl.client.event;


public class StartProxy extends AbstractProxyInitializationEvent {

    public StartProxy(RequireProxy event) {
        super(event);
    }

}
