/*
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
package org.jboss.arquillian.warp.impl.server.enrichment;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.impl.server.inspection.PayloadRegistry;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public class DefaultHttpResponseEnricher implements HttpResponseEnricher {

    private static final Logger log = Logger.getLogger(DefaultHttpResponseEnricher.class.getName());

    @Inject
    private Instance<RequestPayload> requestPayload;

    @Inject
    private Instance<ResponsePayload> responsePayload;

    @Inject
    private Instance<HttpServletResponse> response;

    @Inject
    private Instance<PayloadRegistry> payloadRegistry;

    @Override
    public void enrichResponse() {
        try {
            registerResponsePayload(responsePayload.get());
        } catch (Exception e) {
            log.log(Level.WARNING, "Response enrichment failed", e);

            ResponsePayload exceptionPayload = new ResponsePayload(requestPayload.get().getSerialId());
            try {
                registerResponsePayload(exceptionPayload);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Response enrichment failed to attach enrichment failure", ex);
            }
        }
    }

    private void registerResponsePayload(ResponsePayload payload) throws IOException {
        payloadRegistry.get().registerResponsePayload(payload);
    }
}
