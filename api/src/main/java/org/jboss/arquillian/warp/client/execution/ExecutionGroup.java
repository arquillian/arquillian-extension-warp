package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

public interface ExecutionGroup {
    
    /**
     * Specifies filter which will be used to select which requests will be enriched and verified
     * 
     * @param filter the filter which specifies which requests will be enriched and verified
     * @return the interface for executing single server verification
     */
    GroupAssertionSpecifier filter(RequestFilter<?> filter);

    /**
     * Specifies class of a filter which will be used to select which requests will be enriched and verified
     * 
     * @param filterClass the class of the filter which specifies which requests will be enriched and verified
     * @return the interface for executing single server verification
     */
    GroupAssertionSpecifier filter(Class<RequestFilter<?>> filterClass);
    
    /**
     * Asserts given server state
     * 
     * @param assertions the objects containing assertions which should be verified on the server in the given order of
     *        execution
     * @return the executor of the groups
     */
    GroupsExecutor verify(ServerAssertion... assertion);
    
}
