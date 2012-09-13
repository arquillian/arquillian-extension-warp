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
package org.jboss.arquillian.warp.server.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.server.enrich.HttpServletRequestEnricher;
import org.jboss.arquillian.warp.server.enrich.HttpServletResponseEnricher;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.utils.SerializationUtils;

public class WarpRequestProcessor {

    private static final String DEFAULT_EXTENSION_CLASS = "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";

    private HttpServletRequest request;
    private HttpServletResponse response;

    private NonWritingServletOutputStream stream;
    private NonWritingPrintWriter writer;

    private NonWritingResponseWrapper nonWritingResponse;

    public WarpRequestProcessor(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.nonWritingResponse = new NonWritingResponseWrapper(response);
    }

    public void process(WarpRequest warpRequest, DoFilterCommand filterCommand) throws IOException {

        ResponsePayload responsePayload;
        boolean requestFailed = false;

        try {
            ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));
            Manager manager = builder.create();
            manager.start();
            manager.bind(ApplicationScoped.class, Manager.class, manager);

            WarpLifecycle warpLifecycle = new WarpLifecycle();
            manager.inject(warpLifecycle);

            ServerAssertion serverAssertion = warpRequest.getServerAssertion();

            filterCommand.setRequest(request);
            filterCommand.setResponse(nonWritingResponse);

            HttpServletRequestEnricher.setRequest(request);
            HttpServletResponseEnricher.setResponse(response);

            responsePayload = warpLifecycle.execute(manager, request, filterCommand, serverAssertion);
        } catch (Throwable e) {
            responsePayload = new ResponsePayload(e);
            requestFailed = true;
        } finally {
            HttpServletRequestEnricher.setRequest(null);
            HttpServletResponseEnricher.setResponse(null);
        }

        if (responsePayload.getThrowable() != null) {
            responsePayload.getThrowable().printStackTrace();
        }

        try {
            enrichResponse(response, responsePayload);

            if (writer != null) {
                writer.finallyWriteAndClose(response.getOutputStream());
            }
            if (stream != null) {
                stream.finallyWriteAndClose(response.getOutputStream());
            }

            if (requestFailed) {
                if (!response.isCommitted()) {
                    response.sendError(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, e.getMessage());
        }
    }

    private void enrichResponse(HttpServletResponse response, ResponsePayload payload) throws IOException {

        String enrichment = SerializationUtils.serializeToBase64(payload);

        // set a header with the size of the payload
        response.setHeader(WarpCommons.ENRICHMENT_RESPONSE, Integer.toString(enrichment.length()));

        if (nonWritingResponse.getContentLength() != null) {
            nonWritingResponse.setContentLength(nonWritingResponse.getContentLength() + enrichment.length());
        }

        nonWritingResponse.finalize();

        response.getOutputStream().write(enrichment.getBytes());
    }

    private class NonWritingResponseWrapper extends HttpServletResponseWrapper {

        private Integer contentLength = null;

        public NonWritingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (stream == null) {
                stream = new NonWritingServletOutputStream();
            }
            return stream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                writer = NonWritingPrintWriter.newInstance();
            }
            return writer;
        }

        @Override
        public void setContentLength(int len) {
            this.contentLength = len;
        }

        public Integer getContentLength() {
            return contentLength;
        }

        public void finalize() {
            if (contentLength != null) {
                super.setContentLength(contentLength);
            }
        }
    }
}