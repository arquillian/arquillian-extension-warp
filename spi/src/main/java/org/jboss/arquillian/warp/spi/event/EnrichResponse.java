package org.jboss.arquillian.warp.spi.event;

public interface EnrichResponse<P> {

    P getPayload();
}
