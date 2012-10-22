package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ClientAction;

public interface RequestExecutor {

    ClientActionExecutor execute(ClientAction action);

}
