/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.enrichment;

import java.util.Collection;

import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Service for enriching requests
 *
 * @author Lukas Fryc
 */
public interface HttpRequestEnrichmentService {

    /**
     * Returns payloads associated with given request - if no request matches, then request won't be enriched.
     */
    Collection<RequestPayload> getMatchingPayloads(HttpRequest request);

    /**
     * Enrich request with given payloads.
     */
    void enrichRequest(HttpRequest request, RequestPayload payloads);
}
