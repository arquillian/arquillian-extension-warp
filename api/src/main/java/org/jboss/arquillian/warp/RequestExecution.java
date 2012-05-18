package org.jboss.arquillian.warp;

public interface RequestExecution {
    public <T extends ServerAssertion> T verify(T assertion);
}
