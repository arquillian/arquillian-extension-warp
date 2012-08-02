package org.jboss.arquillian.warp.server.enrich;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class HttpServletRequestEnricher implements ResourceProvider {

    private static ThreadLocal<HttpServletRequest> requestStore = new ThreadLocal<HttpServletRequest>();

    public static void setRequest(HttpServletRequest request) {
        requestStore.set(request);
    }

    @Override
    public boolean canProvide(Class<?> type) {
        return type == HttpServletRequest.class;
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return requestStore.get();
    }
}
