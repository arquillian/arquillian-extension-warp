package org.jboss.arquillian.warp.impl.client.eventbus;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.impl.shared.RemoteOperation;
import org.jboss.arquillian.warp.impl.shared.RemoteOperationService;

public class RemoteOperationServiceOnClient implements RemoteOperationService {

    @Inject
    private Instance<CommandBus> busInstance;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RemoteOperation> T execute(T operation) {
        CommandBus commandBus = busInstance.get();
        return (T) commandBus.executeRemotely(operation);
    }
}
