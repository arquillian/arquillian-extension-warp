package org.jboss.arquillian.warp.impl.shared;

public interface RemoteOperationService {

    public <T extends RemoteOperation> T execute(T operation);
}
