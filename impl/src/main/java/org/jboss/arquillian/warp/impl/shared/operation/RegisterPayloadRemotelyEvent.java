package org.jboss.arquillian.warp.impl.shared.operation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.impl.server.inspection.PayloadRegistry;
import org.jboss.arquillian.warp.impl.shared.RemoteOperation;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;


public class RegisterPayloadRemotelyEvent implements RemoteOperation {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient Instance<PayloadRegistry> registry;

    private String requestPayload;

    public RegisterPayloadRemotelyEvent(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    @Override
    public void execute() {
        RequestPayload payload = SerializationUtils.deserializeFromBase64(requestPayload);
        registry.get().put(payload);
    }
}
