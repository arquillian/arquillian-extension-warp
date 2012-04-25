package org.jboss.arquillian.jsfunitng.lifecycle;

import javax.servlet.ServletRequest;

public class UnbindLifecycleManager<T> {

    private ServletRequest request;
    private Class<T> determinator;
    private T boundObject;

    public UnbindLifecycleManager(ServletRequest request, Class<T> determinator, T boundObject) {
        this.request = request;
        this.determinator = determinator;
        this.boundObject = boundObject;
    }

    public ServletRequest getRequest() {
        return request;
    }

    public Class<T> getDeterminator() {
        return determinator;
    }

    public T getBoundObject() {
        return boundObject;
    }
}
