package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.client.result.WarpResult;

public interface GroupsExecutor {

    /**
     * Executes the client action, applying all the assertions in all given groups
     *
     * @return the result of execution of execution of all specified groups
     */
    WarpResult verifyAll();

    /**
     * Specifies anonymous group of execution - each specified group will be independently filtered and executed, providing
     * interface for verifying different assertions for several requests caused by single client action.
     * 
     * After execution, the details of execution can be retrieved for each group independently by the sequence number given by
     * the order of definition (starting with 0). For result retrival by names, see {@link #group(Object)}.
     * 
     * @return the group executor which specifies what assertions to verify on the server
     */
    ExecutionGroup group();

    /**
     * Specifies named group of execution - each specified group will be independently filtered and executed, providing
     * interface for verifying different assertions for several requests caused by single client action.
     * 
     * After execution, the details of execution can be retrieved for each group independently by the provided identified.
     * 
     * @return the group executor which specifies what assertions to verify on the server
     */
    ExecutionGroup group(Object identifier);
}
