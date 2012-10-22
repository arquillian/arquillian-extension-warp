package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

public interface ClientActionExecutor {
    
    
    
    /**
     * Asserts given server state
     *
     * @param assertion the object containing assertions which should be verified on the server
     * @return the verified server state returned from the server
     */
    <T extends ServerAssertion> T verify(T assertion);
    
    FilterSpecifier filter(RequestFilter<?> filter);

    ExecutionGroup group(Object identifier);
    
}
