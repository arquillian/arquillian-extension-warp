package org.jboss.arquillian.warp.ftest;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;

public class FaviconIgnore implements RequestFilter<HttpRequest> {
    @Override
    public boolean matches(HttpRequest httpRequest) {
        return !httpRequest.getUri().contains("favicon.ico");
    }
}