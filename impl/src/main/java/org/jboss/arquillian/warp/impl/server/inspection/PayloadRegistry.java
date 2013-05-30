package org.jboss.arquillian.warp.impl.server.inspection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.warp.impl.shared.RequestPayload;

public class PayloadRegistry {

    private Map<Long, RequestPayload> requestPayloads = new ConcurrentHashMap<Long, RequestPayload>();

    public void put(RequestPayload requestPayload) {
        long serialId = requestPayload.getSerialId();
        if (requestPayloads.put(serialId, requestPayload) != null) {
            throw new IllegalStateException("The exception payload with serialId " + serialId + " was already registered");
        }
    }

    public RequestPayload get(long serialId) {
        RequestPayload payload = requestPayloads.get(serialId);
        if (payload == null) {
            throw new IllegalStateException("The payload with serialId" + serialId + " was never registered");
        }
        return payload;
    }
}
