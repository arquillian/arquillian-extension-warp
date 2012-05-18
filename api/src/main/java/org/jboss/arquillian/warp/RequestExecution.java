package org.jboss.arquillian.warp;

/**
 * The execution of client action and server assertion.
 * 
 * @author Lukas Fryc
 */
public interface RequestExecution {

    /**
     * Asserts given server state
     * 
     * @param assertion the object containing assertions which should be verified on the server
     * @return the verified server state returned from the server
     */
    public <T extends ServerAssertion> T verify(T assertion);
}
