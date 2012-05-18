package org.jboss.arquillian.warp.shared;

import java.io.Serializable;

import org.jboss.arquillian.warp.ServerAssertion;

public class RequestPayload implements Serializable {

    private static final long serialVersionUID = -5537112559937896153L;

    private ServerAssertion assertion;

    public RequestPayload(ServerAssertion assertion) {
        this.assertion = assertion;
    }

    public ServerAssertion getAssertion() {
        return assertion;
    }
}
