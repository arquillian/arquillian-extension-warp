package org.jboss.arquillian.warp.impl.shared.event;

import org.jboss.arquillian.warp.impl.server.event.WarpRemoteEvent;

public class RegisterPayloadRemotelyEvent extends WarpRemoteEvent {

    private static final long serialVersionUID = 1L;

    private String serializedRequestPayload;

    public RegisterPayloadRemotelyEvent(String serializedRequestPayload) {
        this.serializedRequestPayload = serializedRequestPayload;
    }

    public String getSerializedRequestPayload() {
        return serializedRequestPayload;
    }
}
