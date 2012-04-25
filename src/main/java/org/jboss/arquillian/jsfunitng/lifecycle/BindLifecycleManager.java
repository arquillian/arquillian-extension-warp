package org.jboss.arquillian.jsfunitng.lifecycle;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.spi.Validate;

public class BindLifecycleManager<T> {

    private ServletRequest request;
    private Class<T> determinator;
    private T boundObject;

    public BindLifecycleManager(ServletRequest request, Class<T> determinator, T boundObject) {
        Validate.notNull(request, "request must not be null");
        Validate.notNull(determinator, "determinator must not be null");
        Validate.notNull(boundObject, "boundObject must not be null");
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
