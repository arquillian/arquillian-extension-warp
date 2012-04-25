package org.jboss.arquillian.jsfunitng.request;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.spi.event.Event;

public class RequestEvent implements Event {

    private ServletRequest request;

    public RequestEvent(ServletRequest request) {
        this.request = request;
    }

    public ServletRequest getRequest() {
        return request;
    }
}
