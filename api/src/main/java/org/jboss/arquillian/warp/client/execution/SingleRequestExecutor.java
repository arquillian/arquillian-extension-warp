package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.result.WarpResult;

public interface SingleRequestExecutor {

    /**
     * The key for single request executions
     */
    Object KEY = SingleRequestExecutor.class;
    
    /**
     * Asserts given server state
     * 
     * @param assertion the object containing assertions which should be verified on the server
     * @return the verified server state returned from the server
     */
    <T extends ServerAssertion> T verify(T assertion);

    /**
     * Asserts given server state
     * 
     * @param assertions the objects containing assertions which should be verified on the server in the given order of
     *        execution
     * @return the result of server state verification
     */
    WarpResult verifyAll(ServerAssertion... assertions);
}
