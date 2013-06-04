package org.jboss.arquillian.warp.impl.server.command;

import org.jboss.arquillian.warp.impl.shared.OperationPayload;
import org.jboss.arquillian.warp.impl.shared.RemoteOperation;
import org.jboss.arquillian.warp.impl.shared.RemoteOperationService;

public class RemoteOperationServiceOnServer implements RemoteOperationService {

    private static long TIMEOUT = 30000;

    @Override
    public <T extends RemoteOperation> T execute(T operation) {
        String currentId = CommandEventBusService.getCurrentCall();
        OperationPayload payload = new OperationPayload(operation);
        CommandEventBusService.getEvents().put(currentId, payload);

        long timeoutTime = System.currentTimeMillis() + TIMEOUT;
        while (timeoutTime > System.currentTimeMillis()) {
           OperationPayload responsePayload = CommandEventBusService.getEvents().get(currentId);
           if (responsePayload != null) {
               if (responsePayload.getThrowable() != null) {
                   throw new RuntimeException(responsePayload.getThrowable());
               }
               return (T) responsePayload.getOperation();
           }
           try {
              Thread.sleep(100);
           }
           catch (Exception e) {
              throw new RuntimeException(e);
           }
        }
        throw new RuntimeException("No command response within timeout of " + TIMEOUT + " ms.");
    }

}
