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
package org.jboss.arquillian.warp.impl.client.execution;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseTransformationService;
import org.jboss.arquillian.warp.impl.client.proxy.RealURLToProxyURLMapping;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class DefaultResponseTransformationService implements HttpResponseTransformationService {

    @Inject
    private Instance<RealURLToProxyURLMapping> realToProxyURLMappingInst;

    @Inject
    private Instance<ServiceLoader> services;

    @Override
    public void transformResponse(HttpRequest request, HttpResponse response) {

        String contentTypeHeader = response.getHeader("Content-Type");

        if (contentTypeHeader != null && contentTypeHeader.startsWith("text/")) {

            ContentType contentType = ContentType.parse(contentTypeHeader);
            Charset charset = contentType.getCharset();

            final ChannelBuffer content = response.getContent();

            byte[] data = new byte[content.readableBytes()];
            content.readBytes(data);

            String responseToTransform = createStringFromData(data, charset);
            RealURLToProxyURLMapping mapping = realToProxyURLMappingInst.get();

            for (Map.Entry<URL, URL> entry : mapping.asMap().entrySet()) {
                String realUrl = entry.getKey().toExternalForm();
                String proxyUrl = entry.getValue().toExternalForm();

                int urlStart = responseToTransform.indexOf(realUrl);

                if (urlStart > 0) {
                    responseToTransform = responseToTransform.replace(realUrl, proxyUrl);
                }
            }

            byte[] bytes = createDataFromString(responseToTransform, charset);
            ChannelBuffer transformedContent = ChannelBuffers.dynamicBuffer(bytes.length);
            transformedContent.writeBytes(bytes);

            response.setContent(transformedContent);
            HttpHeaders.setContentLength(response, bytes.length);
        }
    }

    private String createStringFromData(byte[] data, Charset charset) {
        return (charset == null) ? new String(data) : new String(data, charset);
    }

    private byte[] createDataFromString(String string, Charset charset) {
        return (charset == null) ? string.getBytes() : string.getBytes(charset);
    }
}
