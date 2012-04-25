package org.jboss.arquillian.jsfunitng.request;

import javax.servlet.ServletRequest;

public class BeforeRequest extends RequestEvent {

    public BeforeRequest(ServletRequest request) {
        super(request);
    }
    
}
