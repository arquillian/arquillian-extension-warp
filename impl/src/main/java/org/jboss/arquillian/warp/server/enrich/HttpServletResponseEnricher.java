package org.jboss.arquillian.warp.server.enrich;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class HttpServletResponseEnricher implements ResourceProvider {

    private static ThreadLocal<HttpServletResponse> requestStore = new ThreadLocal<HttpServletResponse>();

    public static void setResponse(HttpServletResponse request) {
        requestStore.set(request);
    }

    @Override
    public boolean canProvide(Class<?> type) {
        return type == HttpServletResponse.class;
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return requestStore.get();
    }
}
