package org.jboss.arquillian.warp.extension.phaser.ftest;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;

public class JsfPageRequestFilter implements RequestFilter<HttpRequest> {
    @Override
    public boolean matches(HttpRequest httpRequest) {
        final String uri = httpRequest.getUri();

        return uri.contains(".jsf") && !uri.contains("javax.faces.resource");
    }
}