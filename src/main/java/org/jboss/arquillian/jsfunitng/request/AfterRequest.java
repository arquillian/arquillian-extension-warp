package org.jboss.arquillian.jsfunitng.request;

import javax.servlet.ServletRequest;

public class AfterRequest extends RequestEvent {

    public AfterRequest(ServletRequest request) {
        super(request);
    }
    
}
