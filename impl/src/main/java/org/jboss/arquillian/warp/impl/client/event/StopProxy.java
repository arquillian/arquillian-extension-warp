package org.jboss.arquillian.warp.impl.client.event;

public class StopProxy<T> {

    private T proxy;

    public StopProxy(T proxy) {
        this.proxy = proxy;
    }

    public T getProxy() {
        return proxy;
    }
}
