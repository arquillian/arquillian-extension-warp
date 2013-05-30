package org.jboss.arquillian.warp.impl.server.inspection;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.impl.shared.event.RegisterPayloadRemotelyEvent;

public class InspectionRegistrator {

    public void registerRequestPayload(@Observes RegisterPayloadRemotelyEvent event) {
        System.out.println(event.getSerializedRequestPayload().length());
    }
}
