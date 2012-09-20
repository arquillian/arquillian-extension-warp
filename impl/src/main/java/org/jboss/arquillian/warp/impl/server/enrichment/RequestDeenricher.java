package org.jboss.arquillian.warp.impl.server.enrichment;

public interface RequestDeenricher<P> {

    boolean isEnriched();

    P resolvePayload();
}
