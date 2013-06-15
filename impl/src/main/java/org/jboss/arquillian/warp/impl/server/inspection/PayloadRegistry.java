/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.server.inspection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.warp.impl.server.execution.WarpFilter;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

/**
 * Stores payloads as they are received from Proxy, so that they can be processed during filtering by {@link WarpFilter}.
 *
 * @author Lukas Fryc
 */
public class PayloadRegistry {

    private Map<Long, RequestPayload> requestPayloads = new ConcurrentHashMap<Long, RequestPayload>();
    private Map<Long, ResponsePayload> responsePayloads = new ConcurrentHashMap<Long, ResponsePayload>();

    public void registerRequestPayload(RequestPayload requestPayload) {
        long serialId = requestPayload.getSerialId();
        if (requestPayloads.put(serialId, requestPayload) != null) {
            throw new IllegalStateException("The request payload with serialId " + serialId + " was already registered");
        }
    }

    public RequestPayload retrieveRequestPayload(long serialId) {
        RequestPayload payload = requestPayloads.remove(serialId);
        if (payload == null) {
            throw new IllegalStateException("The request payload with serialId" + serialId + " was never registered");
        }
        return payload;
    }

    public void registerResponsePayload(ResponsePayload responsePayload) {
        long serialId = responsePayload.getSerialId();
        if (responsePayloads.put(serialId, responsePayload) != null) {
            throw new IllegalStateException("The response payload with serialId " + serialId + " was already registered");
        }
    }

    public ResponsePayload retrieveResponsePayload(long serialId) {
        ResponsePayload payload = responsePayloads.remove(serialId);
        if (payload == null) {
            throw new IllegalStateException("The response payload with serialId " + serialId + " was never registered");
        }
        return payload;
    }

}
