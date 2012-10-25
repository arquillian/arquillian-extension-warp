package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ClientAction;

public interface RequestExecutor {

    /**
     * Takes client action which should be executed in order to cause server request.
     *
     * @param action the client action to execute
     * @return {@link RequestExecution} instance
     */
    ClientActionExecutor execute(ClientAction action);
}
