package org.jboss.arquillian.jsfunitng;

import org.jboss.arquillian.jsfunitng.proxy.AssertionExecution;

public class Warp {

    public static AssertionExecution execute(ClientAction action) {
        return new AssertionExecution(action);
    }
}
