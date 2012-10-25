package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ServerAssertion;

public interface GroupAssertionSpecifier {
    
    /**
     * Asserts given server state
     * 
     * @param assertions the objects containing assertions which should be verified on the server in the given order of
     *        execution
     * @return the executor of the groups
     */
    GroupsExecutor verify(ServerAssertion... assertion);
    
}
