package org.jboss.arquillian.warp;

public interface HttpRequest {

    String getMethod();

    String getUri();
}
